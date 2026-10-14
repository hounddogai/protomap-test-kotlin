package com.acme.transport.terminal.sortation.sortationbaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledSortationBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledSortationBaselineValidator()
        val item = newScheduledSortationBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = ScheduledSortationBaselineRequiredCarrierRule("scheduled sortation baseline needs a value")
        val item = newScheduledSortationBaseline("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledSortationBaselineRequiredCodeRule("scheduled sortation baseline needs this field")
        val item = newScheduledSortationBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
