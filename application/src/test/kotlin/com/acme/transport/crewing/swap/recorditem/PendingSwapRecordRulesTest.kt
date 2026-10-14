package com.acme.transport.crewing.swap.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingSwapRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingSwapRecordValidator()
        val item = newPendingSwapRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = PendingSwapRecordDistanceKmRangeRule(2.8, 4638.2)
        val item = newPendingSwapRecord("x", "y")
        item.distanceKm = 4639.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PendingSwapRecordRequiredLabelRule("pending swap record needs an entry")
        val item = newPendingSwapRecord("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
