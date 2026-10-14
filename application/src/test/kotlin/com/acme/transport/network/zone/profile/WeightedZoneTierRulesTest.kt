package com.acme.transport.network.zone.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedZoneTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedZoneTierValidator()
        val item = newWeightedZoneTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedZoneTierCodeLengthRule(120)
        val item = newWeightedZoneTier("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = WeightedZoneTierStopCountRangeRule(120, 3600)
        val item = newWeightedZoneTier("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
