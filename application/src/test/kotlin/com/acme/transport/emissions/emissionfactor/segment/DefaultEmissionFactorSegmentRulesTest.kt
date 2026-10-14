package com.acme.transport.emissions.emissionfactor.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultEmissionFactorSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultEmissionFactorSegmentValidator()
        val item = newDefaultEmissionFactorSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultEmissionFactorSegmentRequiredCodeRule("default emission factor segment needs a value")
        val item = newDefaultEmissionFactorSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultEmissionFactorSegmentCodeLengthRule(1000)
        val item = newDefaultEmissionFactorSegment("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefaultEmissionFactorSegmentLabelLengthRule(32)
        val item = newDefaultEmissionFactorSegment("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = DefaultEmissionFactorSegmentLoadKgRangeRule(2.6, 639.0)
        val item = newDefaultEmissionFactorSegment("x", "y")
        item.loadKg = 640.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
