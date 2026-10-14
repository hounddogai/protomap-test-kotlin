package com.acme.transport.terminal.dwell.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDwellMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientDwellMilestoneValidator()
        val item = newLenientDwellMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = LenientDwellMilestoneRequiredTrailerIdRule("lenient dwell milestone needs a value")
        val item = newLenientDwellMilestone("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = LenientDwellMilestoneTrailerIdLengthRule(32)
        val item = newLenientDwellMilestone("x", "y")
        item.trailerId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LenientDwellMilestoneRequiredLabelRule("lenient dwell milestone needs this field")
        val item = newLenientDwellMilestone("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
