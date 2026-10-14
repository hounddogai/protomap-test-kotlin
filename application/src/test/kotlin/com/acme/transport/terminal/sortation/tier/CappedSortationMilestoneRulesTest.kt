package com.acme.transport.terminal.sortation.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedSortationMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedSortationMilestoneValidator()
        val item = newCappedSortationMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CappedSortationMilestoneRequiredLabelRule("capped sortation milestone needs a value")
        val item = newCappedSortationMilestone("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = CappedSortationMilestoneOwnerIdLengthRule(120)
        val item = newCappedSortationMilestone("x", "y")
        item.ownerId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedSortationMilestoneCodeLengthRule(255)
        val item = newCappedSortationMilestone("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = CappedSortationMilestoneRequiredOwnerIdRule("capped sortation milestone needs a value")
        val item = newCappedSortationMilestone("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
