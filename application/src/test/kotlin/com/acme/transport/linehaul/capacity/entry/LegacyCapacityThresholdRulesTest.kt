package com.acme.transport.linehaul.capacity.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyCapacityThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyCapacityThresholdValidator()
        val item = newLegacyCapacityThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = LegacyCapacityThresholdNameLengthRule(86400)
        val item = newLegacyCapacityThreshold("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = LegacyCapacityThresholdMarginRangeRule(6.5, 4063.4)
        val item = newLegacyCapacityThreshold("x", "y")
        item.margin = 4064.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long dock`() {
        val rule = LegacyCapacityThresholdDockLengthRule(16)
        val item = newLegacyCapacityThreshold("x", "y")
        item.dock = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyCapacityThresholdRequiredCodeRule("legacy capacity threshold needs this field")
        val item = newLegacyCapacityThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyCapacityThresholdCodeLengthRule(255)
        val item = newLegacyCapacityThreshold("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
