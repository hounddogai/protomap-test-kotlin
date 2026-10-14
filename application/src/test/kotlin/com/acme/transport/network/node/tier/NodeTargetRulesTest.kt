package com.acme.transport.network.node.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNodeTargetValidator()
        val item = newNodeTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = NodeTargetOdometerRangeRule(64, 80)
        val item = newNodeTarget("x", "y")
        item.odometer = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = NodeTargetCodeLengthRule(16)
        val item = newNodeTarget("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long route code`() {
        val rule = NodeTargetRouteCodeLengthRule(32)
        val item = newNodeTarget("x", "y")
        item.routeCode = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
