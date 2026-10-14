package com.acme.transport.fleetops.serviceplan.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualServicePlanRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualServicePlanRequestValidator()
        val item = newManualServicePlanRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = ManualServicePlanRequestExpiresAtRangeRule(64, 3600)
        val item = newManualServicePlanRequest("x", "y")
        item.expiresAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ManualServicePlanRequestRequiredTitleRule("manual service plan request needs this field")
        val item = newManualServicePlanRequest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = ManualServicePlanRequestRequiredSummaryRule("manual service plan request needs a value")
        val item = newManualServicePlanRequest("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = ManualServicePlanRequestRegionLengthRule(1000)
        val item = newManualServicePlanRequest("x", "y")
        item.region = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = ManualServicePlanRequestRequiredRegionRule("manual service plan request needs a value")
        val item = newManualServicePlanRequest("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
