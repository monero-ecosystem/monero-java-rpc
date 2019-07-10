package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.List;

import org.junit.Test;

import monero.wallet.MoneroWallet;
import monero.wallet.model.MoneroAccount;
import monero.wallet.model.MoneroIncomingTransfer;
import monero.wallet.model.MoneroOutgoingTransfer;
import monero.wallet.model.MoneroSubaddress;
import monero.wallet.model.MoneroTxWallet;
import utils.TestUtils;

/**
 * Compares two wallets for equality using only on-chain data.
 * 
 * For this test, wallet 2 is assumed to have been synced after wallet 1 so its height may be greater.
 * 
 * The RPC and JNI wallets are tested by default unless overriden by subclassing or using the setters.
 */
public class TestMoneroWalletsEqual {
  
  private MoneroWallet w1;
  private MoneroWallet w2;

  public MoneroWallet getWallet1() {
    return w1;
  }
  
  public TestMoneroWalletsEqual setWallet1(MoneroWallet w1) {
    this.w1 = w1;
    return this;
  }
  
  public MoneroWallet getWallet2() {
    return w2;
  }
  
  public TestMoneroWalletsEqual setWallet2(MoneroWallet w2) {
    this.w2 = w2;
    return this;
  }
  
  @Test
  public void testWalletsEqualOnChain() {
    if (w1 == null) w1 = TestUtils.getWalletRpc();
    if (w2 == null) w2 = TestUtils.getWalletJni();
    assertTrue(w1.getHeight() <= w2.getHeight());
    assertEquals(w1.getMnemonic(), w2.getMnemonic());
    assertEquals(w1.getPrimaryAddress(), w2.getPrimaryAddress());
    assertEquals(w1.getPrivateViewKey(), w2.getPrivateViewKey());
    assertEquals(w1.getPrivateSpendKey(), w2.getPrivateSpendKey());
    if (!w1.getBalance().equals(w2.getBalance())) {
      System.out.println("WARNING: balances are not equal, attempting to re-sync one time");
      w1.sync();
      w2.sync();
    }
    assertEquals(w1.getBalance(), w2.getBalance());
    assertEquals(w1.getUnlockedBalance(), w2.getUnlockedBalance());
    testAccountsEqualOnChain(w1.getAccounts(true), w2.getAccounts(true));
    testTxWalletsEqualOnChain(w1.getTxs(), w2.getTxs());
  }
  
  protected void testAccountsEqualOnChain(List<MoneroAccount> accounts1, List<MoneroAccount> accounts2) {
    for (int i = 0; i < Math.max(accounts1.size(), accounts2.size()); i++) {
      if (i < accounts1.size() && i < accounts2.size()) {
        testAccountsEqualOnChain(accounts1.get(i), accounts2.get(i));
      } else if (i >= accounts1.size()) {
        for (int j = i; j < accounts2.size(); j++) {
          assertEquals(BigInteger.valueOf(0), accounts2.get(j).getBalance());
          assertEquals(1, accounts2.get(j).getSubaddresses().size());
        }
        return;
      } else {
        for (int j = i; j < accounts1.size(); j++) {
          assertEquals(BigInteger.valueOf(0), accounts1.get(j).getBalance());
          assertEquals(1, accounts1.get(j).getSubaddresses().size());
        }
        return;
      }
    }
  }
  
  protected void testAccountsEqualOnChain(MoneroAccount account1, MoneroAccount account2) {
    
    // nullify off-chain data for comparison
    List<MoneroSubaddress> subaddresses1 = account1.getSubaddresses();
    List<MoneroSubaddress> subaddresses2 = account2.getSubaddresses();
    account1.setSubaddresses(null);
    account2.setSubaddresses(null);
    account1.setLabel(null);
    account2.setLabel(null);
    account1.setTag(null);
    account2.setTag(null);
    
    // test account equality
    assertEquals(account1, account2);
    testSubaddressesEqualOnChain(subaddresses1, subaddresses2);
  }
  
  protected void testSubaddressesEqualOnChain(List<MoneroSubaddress> subaddresses1, List<MoneroSubaddress> subaddresses2) {
    for (int i = 0; i < Math.max(subaddresses1.size(), subaddresses2.size()); i++) {
      if (i < subaddresses1.size() && i < subaddresses2.size()) {
        testSubaddressesEqualOnChain(subaddresses1.get(i), subaddresses2.get(i));
      } else if (i >= subaddresses1.size()) {
        for (int j = i; j < subaddresses2.size(); j++) {
          assertEquals(BigInteger.valueOf(0), subaddresses2.get(j).getBalance());
          assertFalse(subaddresses2.get(j).getIsUsed());
        }
        return;
      } else {
        for (int j = i; j < subaddresses1.size(); j++) {
          assertEquals(BigInteger.valueOf(0), subaddresses1.get(i).getBalance());
          assertFalse(subaddresses1.get(j).getIsUsed());
        }
        return;
      }
    }
  }
  
  protected void testSubaddressesEqualOnChain(MoneroSubaddress subaddress1, MoneroSubaddress subaddress2) {
    
    // nullify off-chain data for comparison
    subaddress1.setLabel(null);
    subaddress2.setLabel(null);
    
    // test subaddress equality
    if (!subaddress1.equals(subaddress2)) {
      System.out.println("These subaddresses are not equal:");
      System.out.println(subaddress1);
      System.out.println(subaddress2);
      subaddress1.equals(subaddress2);
    }
    assertEquals(subaddress1, subaddress2);
  }
  
  protected void testTxWalletsEqualOnChain(List<MoneroTxWallet> txs1, List<MoneroTxWallet> txs2) {
    assertEquals(txs1.size(), txs2.size());
    for (int i = 0; i < txs1.size(); i++) {
      testTxWalletsOnChain(txs1.get(i), txs2.get(i));
    }
  }
  
  protected void testTxWalletsOnChain(MoneroTxWallet tx1, MoneroTxWallet tx2) {
    
    // nullify off-chain data for comparison
    List<MoneroIncomingTransfer> inTransfers1 = tx1.getIncomingTransfers();
    List<MoneroIncomingTransfer> inTransfers2 = tx2.getIncomingTransfers();
    MoneroOutgoingTransfer outTransfer1 = tx1.getOutgoingTransfer();
    MoneroOutgoingTransfer outTransfer2 = tx2.getOutgoingTransfer();
    tx1.setNote(null);
    tx2.setNote(null);
    
    // test tx equality
    assertEquals(tx1.getBlock(), tx2.getBlock());
    assertEquals(tx1, tx2);
    
    // TODO: compare transfers
    throw new RuntimeException("Not implemented");
  }
}
