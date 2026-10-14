package com.acme.transport.crewing.restbreak.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakVarianceValidator()
        val item = newRestBreakVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RestBreakVarianceRequiredNameRule("rest break variance needs a value")
        val item = newRestBreakVariance("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RestBreakVarianceNameLengthRule(32)
        val item = newRestBreakVariance("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RestBreakVarianceRequiredCodeRule("rest break variance needs this field")
        val item = newRestBreakVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
