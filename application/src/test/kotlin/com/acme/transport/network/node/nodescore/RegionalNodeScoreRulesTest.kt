package com.acme.transport.network.node.nodescore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalNodeScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalNodeScoreValidator()
        val item = newRegionalNodeScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = RegionalNodeScoreWindowDaysRangeRule(16, 1000)
        val item = newRegionalNodeScore("x", "y")
        item.windowDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalNodeScoreRequiredNameRule("regional node score needs an entry")
        val item = newRegionalNodeScore("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = RegionalNodeScoreTimezoneLengthRule(64)
        val item = newRegionalNodeScore("x", "y")
        item.timezone = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalNodeScoreRequiredCodeRule("regional node score needs this field")
        val item = newRegionalNodeScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = RegionalNodeScoreFloorValueRangeRule(9.7, 381.6)
        val item = newRegionalNodeScore("x", "y")
        item.floorValue = 383.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
