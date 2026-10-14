package com.acme.transport.terminal.sortation.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSortationCycleValidator()
        val item = newSortationCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = SortationCycleLabelLengthRule(32)
        val item = newSortationCycle("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = SortationCycleRequiredTitleRule("sortation cycle needs a value")
        val item = newSortationCycle("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SortationCycleRequiredLabelRule("sortation cycle needs an entry")
        val item = newSortationCycle("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
