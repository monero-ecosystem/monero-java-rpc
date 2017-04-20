package test;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import utils.TestUtils;
import wallet.MoneroAddress;
import wallet.MoneroPayment;
import wallet.MoneroTransaction;
import wallet.MoneroWallet;

/**
 * Tests sending transactions within a Monero wallet.
 * 
 * These tests are separated since they rely on a balance and initiate transactions on the blockchain.
 * 
 * @author woodser
 */
public class TestMoneroWalletTransactions {
  
  private static final BigInteger FEE = null;
  private static final Integer MIXIN = 6;
  private static final int UNLOCKED_DIVISOR = 20;
  
  private MoneroWallet wallet;

  @Before
  public void setup() throws Exception {
    wallet = TestUtils.getWallet();
  }

  @Test
  public void testTransferPayment() {
    
    // get balance before
    BigInteger balanceBefore = wallet.getBalance();
    BigInteger unlockedBalanceBefore = wallet.getUnlockedBalance();
    
    // send to self
    MoneroAddress address = wallet.getStandardAddress();
    BigInteger sendAmount = unlockedBalanceBefore.divide(BigInteger.valueOf(UNLOCKED_DIVISOR));
    MoneroTransaction tx = wallet.transfer(address.toString(), sendAmount, null, FEE, MIXIN, 0);
    
    // test transaction
    assertNotNull(tx.getPayments());
    assertEquals(1, tx.getPayments().size());
    assertTrue(tx.getFee().longValue() > 0);
    assertEquals(MIXIN, tx.getMixin());
    assertNotNull(tx.getTxKey());
    assertNotNull(tx.getTxHash());
    assertNull(tx.getSize());
    assertNull(tx.getType());
    assertNull(tx.getBlockHeight());
    
    // test payments
    for (MoneroPayment payment : tx.getPayments()) {
      assertEquals(address.toString(), payment.getAddress());
      assertEquals(sendAmount, payment.getAmount());
      assertTrue(tx == payment.getTransaction());
    }
    
    // test wallet balance
    assertTrue(wallet.getBalance().longValue() < balanceBefore.longValue());
    assertTrue(wallet.getUnlockedBalance().longValue() < unlockedBalanceBefore.longValue());
  }
  
  @Test
  public void testTransferPayments() {
    
    // get balance and address
    BigInteger balanceBefore = wallet.getBalance();
    BigInteger unlockedBalanceBefore = wallet.getUnlockedBalance();
    MoneroAddress address = wallet.getStandardAddress();
    
    // create payments to send
    int numPayments = 3;
    BigInteger sendAmount = unlockedBalanceBefore.divide(BigInteger.valueOf(numPayments + UNLOCKED_DIVISOR));
    List<MoneroPayment> payments = new ArrayList<MoneroPayment>();
    for (int i = 0; i < numPayments; i++) {
      payments.add(new MoneroPayment(address.toString(), sendAmount));
    }
    
    // send payments
    MoneroTransaction tx = wallet.transfer(payments, null, FEE, MIXIN, 0);
    
    // test transaction
    assertNotNull(tx.getPayments());
    assertEquals(numPayments, tx.getPayments().size());
    assertTrue(tx.getFee().longValue() > 0);
    assertEquals(MIXIN, tx.getMixin());
    assertNotNull(tx.getTxKey());
    assertNotNull(tx.getTxHash());
    assertNull(tx.getSize());
    assertNull(tx.getType());
    assertNull(tx.getBlockHeight());
    
    // test payments
    for (MoneroPayment payment : tx.getPayments()) {
      assertEquals(address.toString(), payment.getAddress());
      assertEquals(sendAmount, payment.getAmount());
      assertTrue(tx == payment.getTransaction());
    }
    
    // test wallet balance
    assertTrue(wallet.getBalance().longValue() < balanceBefore.longValue());
    assertTrue(wallet.getUnlockedBalance().longValue() < unlockedBalanceBefore.longValue());
  }
  
  @Test
  public void testTransferSplit() {
    
    // get balance and address
    BigInteger balanceBefore = wallet.getBalance();
    BigInteger unlockedBalanceBefore = wallet.getUnlockedBalance();
    MoneroAddress address = wallet.getStandardAddress();
    
    // create payments to send
    int numPayments = 3;
    BigInteger sendAmount = unlockedBalanceBefore.divide(BigInteger.valueOf(numPayments + UNLOCKED_DIVISOR));
    List<MoneroPayment> payments = new ArrayList<MoneroPayment>();
    for (int i = 0; i < numPayments; i++) {
      payments.add(new MoneroPayment(address.toString(), sendAmount));
    }
    
    // send payments
    List<MoneroTransaction> txs = wallet.transferSplit(payments, null, FEE, MIXIN, 0, true);
    
    // test transactions
    for (MoneroTransaction tx : txs) {
      assertNull(tx.getPayments());
      assertTrue(tx.getFee().longValue() > 0);
      assertEquals(MIXIN, tx.getMixin());
      assertNull(tx.getTxKey());
      assertNotNull(tx.getTxHash());
      assertNull(tx.getBlockHeight());
      assertNull(tx.getSize());
      assertNull(tx.getType());
      assertNull(tx.getBlockHeight());
    }
    
    // test wallet balance
    assertTrue(wallet.getBalance().longValue() < balanceBefore.longValue());
    assertTrue(wallet.getUnlockedBalance().longValue() < unlockedBalanceBefore.longValue());
  }

  @Test
  public void testSweepDust() {
    List<MoneroTransaction> txs = wallet.sweepDust();
    for (MoneroTransaction tx : txs) {
      assertNull(tx.getPayments());
      assertNull(tx.getFee());
      assertNull(tx.getMixin());
      assertNull(tx.getTxKey());
      assertNotNull(tx.getTxHash());
      assertNull(tx.getSize());
      assertNull(tx.getType());
      assertNull(tx.getBlockHeight());
    }
  }

  @Test
  public void testGetTransactions() {
    
    // test all transactions
    List<MoneroTransaction> txs = wallet.getTransactions();
    assertTrue(txs != null);
    assertFalse(txs.isEmpty()); // must test at least one transaction
    for (MoneroTransaction tx : txs) {
      assertNotNull(tx.getType());
    }
    
    // TODO: test assertions on specific transactions like block height > 0 for incoming
    fail("Not yet implemented");
  }
}
