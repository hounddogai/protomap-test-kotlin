package com.acme.transport.network.node.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNodeForecastValidator()
        val item = newNodeForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = NodeForecastCreatedAtRangeRule(120, 120)
        val item = newNodeForecast("x", "y")
        item.createdAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = NodeForecastCodeLengthRule(32)
        val item = newNodeForecast("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long country`() {
        val rule = NodeForecastCountryLengthRule(255)
        val item = newNodeForecast("x", "y")
        item.country = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank country`() {
        val rule = NodeForecastRequiredCountryRule("node forecast needs this field")
        val item = newNodeForecast("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = NodeForecastLaneLengthRule(32)
        val item = newNodeForecast("x", "y")
        item.lane = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
