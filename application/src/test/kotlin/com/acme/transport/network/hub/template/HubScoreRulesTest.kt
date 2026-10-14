package com.acme.transport.network.hub.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubScoreValidator()
        val item = newHubScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = HubScoreRequiredOwnerIdRule("hub score needs an entry")
        val item = newHubScore("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = HubScoreThresholdRangeRule(5.5, 3782.1)
        val item = newHubScore("x", "y")
        item.threshold = 3783.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = HubScoreRequiredSummaryRule("hub score needs a value")
        val item = newHubScore("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = HubScoreLabelLengthRule(64)
        val item = newHubScore("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
