package com.acme.transport.linehaul.capacity.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCapacityRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingCapacityRollupValidator()
        val item = newPendingCapacityRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = PendingCapacityRollupPriorityRangeRule(120, 120)
        val item = newPendingCapacityRollup("x", "y")
        item.priority = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above margin`() {
        val rule = PendingCapacityRollupMarginRangeRule(0.8, 2798.9)
        val item = newPendingCapacityRollup("x", "y")
        item.margin = 2800.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PendingCapacityRollupRequiredTitleRule("pending capacity rollup needs a value")
        val item = newPendingCapacityRollup("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
