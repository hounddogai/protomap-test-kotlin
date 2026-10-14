package com.acme.transport.network.node.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingNodeRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingNodeRollupValidator()
        val item = newPendingNodeRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = PendingNodeRollupSequenceRangeRule(16, 120)
        val item = newPendingNodeRollup("x", "y")
        item.sequence = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingNodeRollupRequiredCodeRule("pending node rollup needs an entry")
        val item = newPendingNodeRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = PendingNodeRollupSourceLengthRule(1000)
        val item = newPendingNodeRollup("x", "y")
        item.source = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = PendingNodeRollupBatchSizeRangeRule(16, 32)
        val item = newPendingNodeRollup("x", "y")
        item.batchSize = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
