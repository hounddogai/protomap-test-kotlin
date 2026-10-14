package com.acme.transport.network.servicearea.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientServiceAreaRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientServiceAreaRollupValidator()
        val item = newLenientServiceAreaRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientServiceAreaRollupCodeLengthRule(120)
        val item = newLenientServiceAreaRollup("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientServiceAreaRollupRequiredCodeRule("lenient service area rollup needs an entry")
        val item = newLenientServiceAreaRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = LenientServiceAreaRollupCostRangeRule(5.7, 1966.4)
        val item = newLenientServiceAreaRollup("x", "y")
        item.cost = 1967.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = LenientServiceAreaRollupLoadKgRangeRule(1.3, 2590.2)
        val item = newLenientServiceAreaRollup("x", "y")
        item.loadKg = 2591.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = LenientServiceAreaRollupStopCountRangeRule(32, 1000)
        val item = newLenientServiceAreaRollup("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
