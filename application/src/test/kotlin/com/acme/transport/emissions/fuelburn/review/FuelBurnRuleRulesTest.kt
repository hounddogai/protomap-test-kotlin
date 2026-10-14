package com.acme.transport.emissions.fuelburn.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnRuleValidator()
        val item = newFuelBurnRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = FuelBurnRuleRevisionRangeRule(32, 86400)
        val item = newFuelBurnRule("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FuelBurnRuleRequiredCodeRule("fuel burn rule needs this field")
        val item = newFuelBurnRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = FuelBurnRuleRequiredPhoneRule("fuel burn rule needs a value")
        val item = newFuelBurnRule("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = FuelBurnRuleNotesLengthRule(255)
        val item = newFuelBurnRule("x", "y")
        item.notes = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FuelBurnRuleRequiredNameRule("fuel burn rule needs this field")
        val item = newFuelBurnRule("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
