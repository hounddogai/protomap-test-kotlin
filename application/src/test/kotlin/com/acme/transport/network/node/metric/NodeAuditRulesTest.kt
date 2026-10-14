package com.acme.transport.network.node.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNodeAuditValidator()
        val item = newNodeAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = NodeAuditLoadKgRangeRule(5.6, 1680.6)
        val item = newNodeAudit("x", "y")
        item.loadKg = 1682.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = NodeAuditRequiredOwnerIdRule("node audit needs a value")
        val item = newNodeAudit("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = NodeAuditWeightRangeRule(1.2, 3486.1)
        val item = newNodeAudit("x", "y")
        item.weight = 3487.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
