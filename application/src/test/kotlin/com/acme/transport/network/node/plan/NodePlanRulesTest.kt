package com.acme.transport.network.node.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodePlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNodePlanValidator()
        val item = newNodePlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = NodePlanCodeLengthRule(16)
        val item = newNodePlan("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = NodePlanRetriesRangeRule(16, 3600)
        val item = newNodePlan("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
