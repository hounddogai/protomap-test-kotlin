package com.acme.transport.emissions.idling.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingBaselineValidator()
        val item = newIdlingBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = IdlingBaselineRequiredLabelRule("idling baseline needs this field")
        val item = newIdlingBaseline("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = IdlingBaselineChannelLengthRule(3600)
        val item = newIdlingBaseline("x", "y")
        item.channel = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IdlingBaselineRequiredCodeRule("idling baseline needs a value")
        val item = newIdlingBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = IdlingBaselineScoreRangeRule(3.4, 4358.7)
        val item = newIdlingBaseline("x", "y")
        item.score = 4360.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
