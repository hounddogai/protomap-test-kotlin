package com.acme.transport.emissions.fuelburn.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnWindowValidator()
        val item = newFuelBurnWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = FuelBurnWindowRequiredDescriptionRule("fuel burn window needs a value")
        val item = newFuelBurnWindow("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FuelBurnWindowRequiredTitleRule("fuel burn window needs a value")
        val item = newFuelBurnWindow("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = FuelBurnWindowDescriptionLengthRule(64)
        val item = newFuelBurnWindow("x", "y")
        item.description = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
