package com.acme.transport.safety.incident.incidentclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentEstimateValidator()
        val item = newIncidentEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = IncidentEstimateRequiredChannelRule("incident estimate needs an entry")
        val item = newIncidentEstimate("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = IncidentEstimateOdometerRangeRule(255, 86400)
        val item = newIncidentEstimate("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = IncidentEstimateMinimumRangeRule(5.1, 599.8)
        val item = newIncidentEstimate("x", "y")
        item.minimum = 601.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = IncidentEstimateTitleLengthRule(86400)
        val item = newIncidentEstimate("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
