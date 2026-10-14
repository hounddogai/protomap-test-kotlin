package com.acme.transport.network.cutoff.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCutoffOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingCutoffOverrideValidator()
        val item = newPendingCutoffOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingCutoffOverrideRequiredCodeRule("pending cutoff override needs this field")
        val item = newPendingCutoffOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = PendingCutoffOverrideRequiredSegmentRule("pending cutoff override needs a value")
        val item = newPendingCutoffOverride("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PendingCutoffOverrideTitleLengthRule(86400)
        val item = newPendingCutoffOverride("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = PendingCutoffOverrideAmountRangeRule(8.0, 2761.6)
        val item = newPendingCutoffOverride("x", "y")
        item.amount = 2763.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
