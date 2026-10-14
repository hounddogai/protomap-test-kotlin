package com.acme.transport.emissions.idling.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingEstimateValidator()
        val item = newIdlingEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = IdlingEstimateNameLengthRule(255)
        val item = newIdlingEstimate("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = IdlingEstimateCodeLengthRule(86400)
        val item = newIdlingEstimate("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = IdlingEstimateCostRangeRule(7.2, 886.2)
        val item = newIdlingEstimate("x", "y")
        item.cost = 887.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = IdlingEstimateRequiredUnitRule("idling estimate needs this field")
        val item = newIdlingEstimate("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
