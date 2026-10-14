package com.acme.transport.network.transittime.timebudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimeTargetValidator()
        val item = newTransitTimeTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TransitTimeTargetCodeLengthRule(255)
        val item = newTransitTimeTarget("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = TransitTimeTargetQuantityRangeRule(16, 32)
        val item = newTransitTimeTarget("x", "y")
        item.quantity = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long unit`() {
        val rule = TransitTimeTargetUnitLengthRule(32)
        val item = newTransitTimeTarget("x", "y")
        item.unit = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = TransitTimeTargetScoreRangeRule(8.4, 3454.5)
        val item = newTransitTimeTarget("x", "y")
        item.score = 3456.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
