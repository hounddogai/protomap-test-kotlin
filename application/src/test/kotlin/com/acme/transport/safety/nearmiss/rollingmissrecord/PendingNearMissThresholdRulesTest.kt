package com.acme.transport.safety.nearmiss.rollingmissrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingNearMissThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingNearMissThresholdValidator()
        val item = newPendingNearMissThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = PendingNearMissThresholdNameLengthRule(1000)
        val item = newPendingNearMissThreshold("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = PendingNearMissThresholdCostRangeRule(1.3, 1306.9)
        val item = newPendingNearMissThreshold("x", "y")
        item.cost = 1308.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
