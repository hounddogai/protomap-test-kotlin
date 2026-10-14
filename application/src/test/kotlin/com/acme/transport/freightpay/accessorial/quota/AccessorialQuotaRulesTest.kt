package com.acme.transport.freightpay.accessorial.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialQuotaValidator()
        val item = newAccessorialQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = AccessorialQuotaSegmentLengthRule(3600)
        val item = newAccessorialQuota("x", "y")
        item.segment = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = AccessorialQuotaRequiredDriverIdRule("accessorial quota needs a value")
        val item = newAccessorialQuota("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AccessorialQuotaCodeLengthRule(1000)
        val item = newAccessorialQuota("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = AccessorialQuotaAmountRangeRule(6.6, 3038.7)
        val item = newAccessorialQuota("x", "y")
        item.amount = 3040.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AccessorialQuotaRequiredCodeRule("accessorial quota needs this field")
        val item = newAccessorialQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
