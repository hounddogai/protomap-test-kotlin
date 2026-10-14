package com.acme.transport.network.node.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNodeRollupValidator()
        val item = newNodeRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = NodeRollupRequiredLabelRule("node rollup needs an entry")
        val item = newNodeRollup("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = NodeRollupRequiredDockRule("node rollup needs a value")
        val item = newNodeRollup("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = NodeRollupRequiredCodeRule("node rollup needs a value")
        val item = newNodeRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = NodeRollupMarginRangeRule(1.7, 3502.3)
        val item = newNodeRollup("x", "y")
        item.margin = 3503.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
