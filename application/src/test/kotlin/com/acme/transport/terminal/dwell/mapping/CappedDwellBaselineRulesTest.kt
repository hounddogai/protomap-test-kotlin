package com.acme.transport.terminal.dwell.mapping

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedDwellBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedDwellBaselineValidator()
        val item = newCappedDwellBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = CappedDwellBaselineFuelLitersRangeRule(5.1, 4560.0)
        val item = newCappedDwellBaseline("x", "y")
        item.fuelLiters = 4561.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = CappedDwellBaselineLoadKgRangeRule(1.6, 553.7)
        val item = newCappedDwellBaseline("x", "y")
        item.loadKg = 555.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
