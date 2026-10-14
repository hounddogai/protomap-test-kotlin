package com.acme.transport.safety.violation.violationpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedViolationSummaryTest {
    @Test
    fun `starts in open`() {
        val item = newWeightedViolationSummary("code-556", "name-988")
        assertEquals(WeightedViolationSummaryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newWeightedViolationSummary("code-556", "name-988")
        try {
            item.moveTo(WeightedViolationSummaryStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedViolationSummaryStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newWeightedViolationSummary("code-556", "name-988")
        item.status = WeightedViolationSummaryStatus.ARCHIVED
        assertFails("move") { item.moveTo(WeightedViolationSummaryStatus.OPEN) }
    }
}
