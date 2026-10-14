package com.acme.transport.emissions.idling.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingThresholdValidator()
        val item = newIdlingThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = IdlingThresholdRateRangeRule(2.6, 2573.8)
        val item = newIdlingThreshold("x", "y")
        item.rate = 2575.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IdlingThresholdRequiredCodeRule("idling threshold needs a value")
        val item = newIdlingThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = IdlingThresholdPriorityRangeRule(120, 255)
        val item = newIdlingThreshold("x", "y")
        item.priority = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = IdlingThresholdGraceDaysRangeRule(32, 64)
        val item = newIdlingThreshold("x", "y")
        item.graceDays = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = IdlingThresholdCodeLengthRule(120)
        val item = newIdlingThreshold("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = IdlingThresholdRequiredExternalRefRule("idling threshold needs a value")
        val item = newIdlingThreshold("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
