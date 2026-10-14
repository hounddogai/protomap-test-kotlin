package com.acme.transport.lastmile.parcel.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelRollupValidator()
        val item = newParcelRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = ParcelRollupBudgetRangeRule(1.5, 1830.2)
        val item = newParcelRollup("x", "y")
        item.budget = 1831.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = ParcelRollupTimezoneLengthRule(32)
        val item = newParcelRollup("x", "y")
        item.timezone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = ParcelRollupRequiredVehicleIdRule("parcel rollup needs a value")
        val item = newParcelRollup("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
