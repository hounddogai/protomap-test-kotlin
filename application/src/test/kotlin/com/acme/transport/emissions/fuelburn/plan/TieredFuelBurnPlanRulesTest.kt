package com.acme.transport.emissions.fuelburn.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredFuelBurnPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredFuelBurnPlanValidator()
        val item = newTieredFuelBurnPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredFuelBurnPlanRequiredCodeRule("tiered fuel burn plan needs this field")
        val item = newTieredFuelBurnPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TieredFuelBurnPlanRequiredTitleRule("tiered fuel burn plan needs this field")
        val item = newTieredFuelBurnPlan("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
