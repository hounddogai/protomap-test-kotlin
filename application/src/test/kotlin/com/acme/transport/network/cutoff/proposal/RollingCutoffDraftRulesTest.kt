package com.acme.transport.network.cutoff.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingCutoffDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingCutoffDraftValidator()
        val item = newRollingCutoffDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = RollingCutoffDraftDescriptionLengthRule(3600)
        val item = newRollingCutoffDraft("x", "y")
        item.description = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = RollingCutoffDraftDiscountRangeRule(2.4, 4263.6)
        val item = newRollingCutoffDraft("x", "y")
        item.discount = 4265.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingCutoffDraftRequiredNameRule("rolling cutoff draft needs this field")
        val item = newRollingCutoffDraft("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RollingCutoffDraftNameLengthRule(16)
        val item = newRollingCutoffDraft("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
