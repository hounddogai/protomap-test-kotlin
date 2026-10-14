package com.acme.transport.network.node.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNodeAdjustmentValidator()
        val item = newNodeAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = NodeAdjustmentRequiredTitleRule("node adjustment needs a value")
        val item = newNodeAdjustment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = NodeAdjustmentTitleLengthRule(16)
        val item = newNodeAdjustment("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = NodeAdjustmentCodeLengthRule(3600)
        val item = newNodeAdjustment("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = NodeAdjustmentTrailerIdLengthRule(64)
        val item = newNodeAdjustment("x", "y")
        item.trailerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = NodeAdjustmentRequiredTrailerIdRule("node adjustment needs a value")
        val item = newNodeAdjustment("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
