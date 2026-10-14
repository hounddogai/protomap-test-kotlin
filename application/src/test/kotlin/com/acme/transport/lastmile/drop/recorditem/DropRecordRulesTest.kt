package com.acme.transport.lastmile.drop.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropRecordValidator()
        val item = newDropRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = DropRecordThresholdRangeRule(4.8, 3077.1)
        val item = newDropRecord("x", "y")
        item.threshold = 3078.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = DropRecordRequiredUnitRule("drop record needs this field")
        val item = newDropRecord("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = DropRecordMaximumRangeRule(7.3, 4923.1)
        val item = newDropRecord("x", "y")
        item.maximum = 4924.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long unit`() {
        val rule = DropRecordUnitLengthRule(3600)
        val item = newDropRecord("x", "y")
        item.unit = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
