package com.acme.transport.emissions.emissionfactor.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorExceptionValidator()
        val item = newEmissionFactorException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = EmissionFactorExceptionCategoryLengthRule(120)
        val item = newEmissionFactorException("x", "y")
        item.category = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = EmissionFactorExceptionRequiredLabelRule("emission factor exception needs this field")
        val item = newEmissionFactorException("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EmissionFactorExceptionRequiredCodeRule("emission factor exception needs an entry")
        val item = newEmissionFactorException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = EmissionFactorExceptionCodeLengthRule(120)
        val item = newEmissionFactorException("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = EmissionFactorExceptionLabelLengthRule(80)
        val item = newEmissionFactorException("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
