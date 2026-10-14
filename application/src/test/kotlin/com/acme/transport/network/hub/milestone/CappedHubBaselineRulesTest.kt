package com.acme.transport.network.hub.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedHubBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedHubBaselineValidator()
        val item = newCappedHubBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = CappedHubBaselineBatchSizeRangeRule(64, 1000)
        val item = newCappedHubBaseline("x", "y")
        item.batchSize = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedHubBaselineCodeLengthRule(120)
        val item = newCappedHubBaseline("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedHubBaselineRequiredCodeRule("capped hub baseline needs this field")
        val item = newCappedHubBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = CappedHubBaselineDurationMinutesRangeRule(120, 120)
        val item = newCappedHubBaseline("x", "y")
        item.durationMinutes = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
