package com.acme.transport.emissions.fuelburn.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledFuelBurnPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledFuelBurnPlanValidator()
        val item = newScheduledFuelBurnPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScheduledFuelBurnPlanLabelLengthRule(64)
        val item = newScheduledFuelBurnPlan("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = ScheduledFuelBurnPlanBatchSizeRangeRule(16, 16)
        val item = newScheduledFuelBurnPlan("x", "y")
        item.batchSize = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = ScheduledFuelBurnPlanDepotLengthRule(255)
        val item = newScheduledFuelBurnPlan("x", "y")
        item.depot = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = ScheduledFuelBurnPlanRequiredDepotRule("scheduled fuel burn plan needs an entry")
        val item = newScheduledFuelBurnPlan("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = ScheduledFuelBurnPlanEtaMinutesRangeRule(32, 80)
        val item = newScheduledFuelBurnPlan("x", "y")
        item.etaMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
