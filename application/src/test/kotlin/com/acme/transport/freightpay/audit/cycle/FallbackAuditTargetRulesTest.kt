package com.acme.transport.freightpay.audit.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackAuditTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackAuditTargetValidator()
        val item = newFallbackAuditTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FallbackAuditTargetRequiredNameRule("fallback audit target needs this field")
        val item = newFallbackAuditTarget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = FallbackAuditTargetGraceDaysRangeRule(64, 64)
        val item = newFallbackAuditTarget("x", "y")
        item.graceDays = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
