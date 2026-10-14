package com.acme.transport.crewing.roster.rostersummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedRosterSummaryTest {
    @Test
    fun `starts in created`() {
        val item = newWeightedRosterSummary("code-263", "label-149")
        assertEquals(WeightedRosterSummaryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newWeightedRosterSummary("code-263", "label-149")
        try {
            item.moveTo(WeightedRosterSummaryStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedRosterSummaryStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newWeightedRosterSummary("code-263", "label-149")
        item.status = WeightedRosterSummaryStatus.RETIRED
        assertFails("move") { item.moveTo(WeightedRosterSummaryStatus.CREATED) }
    }
}
