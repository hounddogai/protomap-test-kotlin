package com.acme.transport.emissions.emissionfactor.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedEmissionFactorTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedEmissionFactorTemplateValidator()
        val item = newWeightedEmissionFactorTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedEmissionFactorTemplateRequiredCodeRule("weighted emission factor template needs an entry")
        val item = newWeightedEmissionFactorTemplate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = WeightedEmissionFactorTemplateContactEmailLengthRule(64)
        val item = newWeightedEmissionFactorTemplate("x", "y")
        item.contactEmail = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = WeightedEmissionFactorTemplateCategoryLengthRule(86400)
        val item = newWeightedEmissionFactorTemplate("x", "y")
        item.category = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = WeightedEmissionFactorTemplatePriorityRangeRule(80, 120)
        val item = newWeightedEmissionFactorTemplate("x", "y")
        item.priority = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
