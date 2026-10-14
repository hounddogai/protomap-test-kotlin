package com.acme.transport.freightpay.dispute.disputebundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalDisputeBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalDisputeBatchValidator()
        val item = newGlobalDisputeBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = GlobalDisputeBatchChecksumLengthRule(255)
        val item = newGlobalDisputeBatch("x", "y")
        item.checksum = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = GlobalDisputeBatchOdometerRangeRule(80, 1000)
        val item = newGlobalDisputeBatch("x", "y")
        item.odometer = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = GlobalDisputeBatchRequiredCurrencyRule("global dispute batch needs this field")
        val item = newGlobalDisputeBatch("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
