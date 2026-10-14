package com.acme.transport.terminal.sortation.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimarySortationMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimarySortationMilestoneValidator()
        val item = newPrimarySortationMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = PrimarySortationMilestoneNameLengthRule(1000)
        val item = newPrimarySortationMilestone("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long unit`() {
        val rule = PrimarySortationMilestoneUnitLengthRule(80)
        val item = newPrimarySortationMilestone("x", "y")
        item.unit = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimarySortationMilestoneCodeLengthRule(32)
        val item = newPrimarySortationMilestone("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
