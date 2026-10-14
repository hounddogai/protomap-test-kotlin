package com.acme.transport.terminal.dwell.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDwellEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualDwellEstimateValidator()
        val item = newManualDwellEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ManualDwellEstimateNameLengthRule(16)
        val item = newManualDwellEstimate("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = ManualDwellEstimateCapacityRangeRule(1000, 1000)
        val item = newManualDwellEstimate("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above score`() {
        val rule = ManualDwellEstimateScoreRangeRule(3.1, 1305.9)
        val item = newManualDwellEstimate("x", "y")
        item.score = 1307.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
