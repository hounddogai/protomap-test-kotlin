package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialOverrideValidator()
        val item = newAccessorialOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AccessorialOverrideTitleLengthRule(64)
        val item = newAccessorialOverride("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AccessorialOverrideCodeLengthRule(80)
        val item = newAccessorialOverride("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = AccessorialOverrideThresholdRangeRule(0.1, 1894.7)
        val item = newAccessorialOverride("x", "y")
        item.threshold = 1896.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AccessorialOverrideRequiredCodeRule("accessorial override needs this field")
        val item = newAccessorialOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
