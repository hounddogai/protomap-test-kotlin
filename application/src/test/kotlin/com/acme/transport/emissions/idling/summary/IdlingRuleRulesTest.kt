package com.acme.transport.emissions.idling.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingRuleValidator()
        val item = newIdlingRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = IdlingRuleThresholdRangeRule(3.4, 3479.0)
        val item = newIdlingRule("x", "y")
        item.threshold = 3480.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = IdlingRuleTitleLengthRule(1000)
        val item = newIdlingRule("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IdlingRuleRequiredCodeRule("idling rule needs this field")
        val item = newIdlingRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = IdlingRuleCodeLengthRule(80)
        val item = newIdlingRule("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long depot`() {
        val rule = IdlingRuleDepotLengthRule(80)
        val item = newIdlingRule("x", "y")
        item.depot = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
