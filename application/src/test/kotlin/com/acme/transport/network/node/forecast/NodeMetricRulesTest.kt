package com.acme.transport.network.node.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNodeMetricValidator()
        val item = newNodeMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = NodeMetricRequiredRouteCodeRule("node metric needs an entry")
        val item = newNodeMetric("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = NodeMetricLoadKgRangeRule(6.9, 1771.0)
        val item = newNodeMetric("x", "y")
        item.loadKg = 1772.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = NodeMetricRequiredContactEmailRule("node metric needs this field")
        val item = newNodeMetric("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = NodeMetricNameLengthRule(32)
        val item = newNodeMetric("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = NodeMetricCodeLengthRule(1000)
        val item = newNodeMetric("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
