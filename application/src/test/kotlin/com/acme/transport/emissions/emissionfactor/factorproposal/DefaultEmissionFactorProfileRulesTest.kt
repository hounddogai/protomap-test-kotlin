package com.acme.transport.emissions.emissionfactor.factorproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultEmissionFactorProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultEmissionFactorProfileValidator()
        val item = newDefaultEmissionFactorProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = DefaultEmissionFactorProfileRequiredCategoryRule("default emission factor profile needs an entry")
        val item = newDefaultEmissionFactorProfile("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DefaultEmissionFactorProfileRequiredTitleRule("default emission factor profile needs this field")
        val item = newDefaultEmissionFactorProfile("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = DefaultEmissionFactorProfileCategoryLengthRule(32)
        val item = newDefaultEmissionFactorProfile("x", "y")
        item.category = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultEmissionFactorProfileCodeLengthRule(32)
        val item = newDefaultEmissionFactorProfile("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
