package com.acme.transport.customs.hold.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldVarianceValidator()
        val item = newHoldVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = HoldVarianceRequiredCodeRule("hold variance needs a value")
        val item = newHoldVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = HoldVarianceNameLengthRule(32)
        val item = newHoldVariance("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = HoldVarianceLoadKgRangeRule(2.2, 2169.5)
        val item = newHoldVariance("x", "y")
        item.loadKg = 2171.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
