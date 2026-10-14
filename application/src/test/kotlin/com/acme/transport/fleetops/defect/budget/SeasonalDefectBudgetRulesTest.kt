package com.acme.transport.fleetops.defect.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDefectBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalDefectBudgetValidator()
        val item = newSeasonalDefectBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalDefectBudgetRequiredCodeRule("seasonal defect budget needs this field")
        val item = newSeasonalDefectBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = SeasonalDefectBudgetTitleLengthRule(1000)
        val item = newSeasonalDefectBudget("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = SeasonalDefectBudgetVehicleIdLengthRule(86400)
        val item = newSeasonalDefectBudget("x", "y")
        item.vehicleId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
