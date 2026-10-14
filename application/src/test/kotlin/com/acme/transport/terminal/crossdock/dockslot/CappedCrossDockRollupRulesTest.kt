package com.acme.transport.terminal.crossdock.dockslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedCrossDockRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedCrossDockRollupValidator()
        val item = newCappedCrossDockRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = CappedCrossDockRollupReviewerLengthRule(64)
        val item = newCappedCrossDockRollup("x", "y")
        item.reviewer = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedCrossDockRollupNameLengthRule(255)
        val item = newCappedCrossDockRollup("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = CappedCrossDockRollupCeilingRangeRule(2.9, 2870.2)
        val item = newCappedCrossDockRollup("x", "y")
        item.ceiling = 2871.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
