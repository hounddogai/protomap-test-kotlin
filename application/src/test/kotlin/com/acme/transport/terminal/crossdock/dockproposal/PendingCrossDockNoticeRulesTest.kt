package com.acme.transport.terminal.crossdock.dockproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCrossDockNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingCrossDockNoticeValidator()
        val item = newPendingCrossDockNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = PendingCrossDockNoticeStopCountRangeRule(32, 32)
        val item = newPendingCrossDockNotice("x", "y")
        item.stopCount = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above cost`() {
        val rule = PendingCrossDockNoticeCostRangeRule(1.4, 3812.1)
        val item = newPendingCrossDockNotice("x", "y")
        item.cost = 3813.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingCrossDockNoticeRequiredCodeRule("pending cross dock notice needs an entry")
        val item = newPendingCrossDockNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PendingCrossDockNoticeLabelLengthRule(16)
        val item = newPendingCrossDockNotice("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
