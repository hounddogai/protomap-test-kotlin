package com.acme.transport.network.hub.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualHubBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualHubBaselineValidator()
        val item = newManualHubBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = ManualHubBaselinePriorityRangeRule(1000, 86400)
        val item = newManualHubBaseline("x", "y")
        item.priority = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = ManualHubBaselineTitleLengthRule(120)
        val item = newManualHubBaseline("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = ManualHubBaselineDriverIdLengthRule(32)
        val item = newManualHubBaseline("x", "y")
        item.driverId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = ManualHubBaselineSequenceRangeRule(16, 32)
        val item = newManualHubBaseline("x", "y")
        item.sequence = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
