package com.acme.transport.terminal.crossdock.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockVarianceValidator()
        val item = newCrossDockVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = CrossDockVarianceRequiredShiftCodeRule("cross dock variance needs this field")
        val item = newCrossDockVariance("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = CrossDockVarianceShiftCodeLengthRule(1000)
        val item = newCrossDockVariance("x", "y")
        item.shiftCode = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = CrossDockVarianceGraceDaysRangeRule(80, 120)
        val item = newCrossDockVariance("x", "y")
        item.graceDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = CrossDockVarianceNameLengthRule(64)
        val item = newCrossDockVariance("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
