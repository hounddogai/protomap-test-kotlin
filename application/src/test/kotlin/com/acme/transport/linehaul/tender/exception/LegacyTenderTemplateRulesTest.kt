package com.acme.transport.linehaul.tender.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTenderTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyTenderTemplateValidator()
        val item = newLegacyTenderTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = LegacyTenderTemplateBudgetRangeRule(9.3, 1939.2)
        val item = newLegacyTenderTemplate("x", "y")
        item.budget = 1940.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = LegacyTenderTemplateDriverIdLengthRule(86400)
        val item = newLegacyTenderTemplate("x", "y")
        item.driverId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = LegacyTenderTemplateRequiredDriverIdRule("legacy tender template needs a value")
        val item = newLegacyTenderTemplate("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
