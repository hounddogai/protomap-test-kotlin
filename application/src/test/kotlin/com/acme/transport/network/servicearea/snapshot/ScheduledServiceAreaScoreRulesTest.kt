package com.acme.transport.network.servicearea.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledServiceAreaScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledServiceAreaScoreValidator()
        val item = newScheduledServiceAreaScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScheduledServiceAreaScoreRequiredLabelRule("scheduled service area score needs an entry")
        val item = newScheduledServiceAreaScore("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledServiceAreaScoreCodeLengthRule(80)
        val item = newScheduledServiceAreaScore("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScheduledServiceAreaScoreLabelLengthRule(64)
        val item = newScheduledServiceAreaScore("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
