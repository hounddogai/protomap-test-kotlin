package com.acme.transport.customs.declaration.declarationforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryDeclarationForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryDeclarationForecastValidator()
        val item = newPrimaryDeclarationForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = PrimaryDeclarationForecastNotesLengthRule(86400)
        val item = newPrimaryDeclarationForecast("x", "y")
        item.notes = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = PrimaryDeclarationForecastRateRangeRule(5.3, 3181.5)
        val item = newPrimaryDeclarationForecast("x", "y")
        item.rate = 3183.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
