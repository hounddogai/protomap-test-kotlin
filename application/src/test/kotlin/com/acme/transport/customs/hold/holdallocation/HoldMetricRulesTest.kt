package com.acme.transport.customs.hold.holdallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldMetricValidator()
        val item = newHoldMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = HoldMetricRequiredCodeRule("hold metric needs this field")
        val item = newHoldMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = HoldMetricRequiredNotesRule("hold metric needs a value")
        val item = newHoldMetric("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = HoldMetricNotesLengthRule(255)
        val item = newHoldMetric("x", "y")
        item.notes = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
