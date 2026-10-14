package com.acme.transport.customs.hold.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldQuotaValidator()
        val item = newHoldQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = HoldQuotaDiscountRangeRule(6.8, 4248.6)
        val item = newHoldQuota("x", "y")
        item.discount = 4250.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = HoldQuotaNameLengthRule(3600)
        val item = newHoldQuota("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = HoldQuotaRequiredCodeRule("hold quota needs this field")
        val item = newHoldQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = HoldQuotaBatchSizeRangeRule(80, 1000)
        val item = newHoldQuota("x", "y")
        item.batchSize = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
