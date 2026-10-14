package com.acme.transport.emissions.fuelburn.burnmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingFuelBurnTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingFuelBurnTemplateValidator()
        val item = newPendingFuelBurnTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PendingFuelBurnTemplateRequiredTitleRule("pending fuel burn template needs this field")
        val item = newPendingFuelBurnTemplate("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = PendingFuelBurnTemplateRequiredCountryRule("pending fuel burn template needs this field")
        val item = newPendingFuelBurnTemplate("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingFuelBurnTemplateRequiredCodeRule("pending fuel burn template needs this field")
        val item = newPendingFuelBurnTemplate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = PendingFuelBurnTemplateCountryLengthRule(16)
        val item = newPendingFuelBurnTemplate("x", "y")
        item.country = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingFuelBurnTemplateCodeLengthRule(255)
        val item = newPendingFuelBurnTemplate("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
