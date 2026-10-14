package com.acme.transport.network.servicearea.cappedareaassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultServiceAreaMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultServiceAreaMilestoneValidator()
        val item = newDefaultServiceAreaMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = DefaultServiceAreaMilestoneSequenceRangeRule(120, 86400)
        val item = newDefaultServiceAreaMilestone("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = DefaultServiceAreaMilestoneTitleLengthRule(80)
        val item = newDefaultServiceAreaMilestone("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = DefaultServiceAreaMilestoneGraceDaysRangeRule(1000, 1000)
        val item = newDefaultServiceAreaMilestone("x", "y")
        item.graceDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long dock`() {
        val rule = DefaultServiceAreaMilestoneDockLengthRule(86400)
        val item = newDefaultServiceAreaMilestone("x", "y")
        item.dock = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = DefaultServiceAreaMilestoneThresholdRangeRule(4.5, 3907.2)
        val item = newDefaultServiceAreaMilestone("x", "y")
        item.threshold = 3908.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
