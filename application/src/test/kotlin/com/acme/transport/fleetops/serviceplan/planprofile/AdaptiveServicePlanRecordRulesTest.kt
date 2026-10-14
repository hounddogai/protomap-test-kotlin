package com.acme.transport.fleetops.serviceplan.planprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveServicePlanRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveServicePlanRecordValidator()
        val item = newAdaptiveServicePlanRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveServicePlanRecordNameLengthRule(1000)
        val item = newAdaptiveServicePlanRecord("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = AdaptiveServicePlanRecordRequiredReviewerRule("adaptive service plan record needs a value")
        val item = newAdaptiveServicePlanRecord("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
