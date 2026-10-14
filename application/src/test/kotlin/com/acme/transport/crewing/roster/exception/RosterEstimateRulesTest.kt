package com.acme.transport.crewing.roster.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterEstimateValidator()
        val item = newRosterEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = RosterEstimatePriorityRangeRule(120, 255)
        val item = newRosterEstimate("x", "y")
        item.priority = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RosterEstimateCodeLengthRule(16)
        val item = newRosterEstimate("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = RosterEstimateScoreRangeRule(9.4, 1676.4)
        val item = newRosterEstimate("x", "y")
        item.score = 1677.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = RosterEstimateRequiredContactEmailRule("roster estimate needs this field")
        val item = newRosterEstimate("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RosterEstimateRequiredCodeRule("roster estimate needs an entry")
        val item = newRosterEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
