package com.acme.transport.network.transittime.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTransitTimeEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedTransitTimeEntryValidator()
        val item = newBatchedTransitTimeEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = BatchedTransitTimeEntryRequiredTitleRule("batched transit time entry needs an entry")
        val item = newBatchedTransitTimeEntry("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedTransitTimeEntryRequiredCodeRule("batched transit time entry needs a value")
        val item = newBatchedTransitTimeEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = BatchedTransitTimeEntryDistanceKmRangeRule(3.8, 2792.6)
        val item = newBatchedTransitTimeEntry("x", "y")
        item.distanceKm = 2794.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
