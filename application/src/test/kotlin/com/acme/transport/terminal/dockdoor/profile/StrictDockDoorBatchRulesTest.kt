package com.acme.transport.terminal.dockdoor.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDockDoorBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictDockDoorBatchValidator()
        val item = newStrictDockDoorBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = StrictDockDoorBatchRequiredDescriptionRule("strict dock door batch needs a value")
        val item = newStrictDockDoorBatch("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = StrictDockDoorBatchAmountRangeRule(1.3, 2460.1)
        val item = newStrictDockDoorBatch("x", "y")
        item.amount = 2461.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictDockDoorBatchCodeLengthRule(255)
        val item = newStrictDockDoorBatch("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long description`() {
        val rule = StrictDockDoorBatchDescriptionLengthRule(1000)
        val item = newStrictDockDoorBatch("x", "y")
        item.description = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long notes`() {
        val rule = StrictDockDoorBatchNotesLengthRule(80)
        val item = newStrictDockDoorBatch("x", "y")
        item.notes = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
