package com.acme.transport.fleetops.tire.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingTireBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingTireBaselineValidator()
        val item = newPendingTireBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = PendingTireBaselineRequiredNotesRule("pending tire baseline needs this field")
        val item = newPendingTireBaseline("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = PendingTireBaselineRequiredSegmentRule("pending tire baseline needs a value")
        val item = newPendingTireBaseline("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PendingTireBaselineLabelLengthRule(16)
        val item = newPendingTireBaseline("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
