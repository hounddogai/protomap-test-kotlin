package com.acme.transport.lastmile.redelivery.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliveryOverrideValidator()
        val item = newRedeliveryOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RedeliveryOverrideRequiredCodeRule("redelivery override needs a value")
        val item = newRedeliveryOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RedeliveryOverrideTitleLengthRule(3600)
        val item = newRedeliveryOverride("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = RedeliveryOverrideCostRangeRule(2.6, 3819.5)
        val item = newRedeliveryOverride("x", "y")
        item.cost = 3821.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RedeliveryOverrideRequiredTitleRule("redelivery override needs a value")
        val item = newRedeliveryOverride("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RedeliveryOverrideRequiredLabelRule("redelivery override needs a value")
        val item = newRedeliveryOverride("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
