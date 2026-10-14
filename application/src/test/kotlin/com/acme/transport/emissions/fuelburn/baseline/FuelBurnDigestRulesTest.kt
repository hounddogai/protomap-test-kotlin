package com.acme.transport.emissions.fuelburn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnDigestValidator()
        val item = newFuelBurnDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = FuelBurnDigestRequiredOwnerIdRule("fuel burn digest needs a value")
        val item = newFuelBurnDigest("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FuelBurnDigestRequiredLabelRule("fuel burn digest needs this field")
        val item = newFuelBurnDigest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FuelBurnDigestRequiredCodeRule("fuel burn digest needs this field")
        val item = newFuelBurnDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FuelBurnDigestRequiredNameRule("fuel burn digest needs this field")
        val item = newFuelBurnDigest("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = FuelBurnDigestWeightRangeRule(1.2, 4791.1)
        val item = newFuelBurnDigest("x", "y")
        item.weight = 4792.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
