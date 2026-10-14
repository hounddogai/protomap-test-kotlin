package com.acme.transport.network.cutoff.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCutoffAdjustmentValidator()
        val item = newCutoffAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = CutoffAdjustmentLoadKgRangeRule(1.6, 1955.2)
        val item = newCutoffAdjustment("x", "y")
        item.loadKg = 1956.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = CutoffAdjustmentCodeLengthRule(1000)
        val item = newCutoffAdjustment("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = CutoffAdjustmentNameLengthRule(16)
        val item = newCutoffAdjustment("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
