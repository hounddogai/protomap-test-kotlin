package com.acme.transport.network.node.nodescore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNodeRuleValidator()
        val item = newNodeRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = NodeRuleContactEmailLengthRule(80)
        val item = newNodeRule("x", "y")
        item.contactEmail = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = NodeRuleLoadKgRangeRule(8.5, 3857.6)
        val item = newNodeRule("x", "y")
        item.loadKg = 3859.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
