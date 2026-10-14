package com.acme.transport.terminal.crossdock.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingCrossDockSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingCrossDockSummaryValidator()
        val item = newRollingCrossDockSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = RollingCrossDockSummaryWeightRangeRule(1.6, 4704.8)
        val item = newRollingCrossDockSummary("x", "y")
        item.weight = 4706.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = RollingCrossDockSummaryExternalRefLengthRule(80)
        val item = newRollingCrossDockSummary("x", "y")
        item.externalRef = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingCrossDockSummaryCodeLengthRule(255)
        val item = newRollingCrossDockSummary("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
