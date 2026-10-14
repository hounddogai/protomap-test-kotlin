package com.acme.transport.crewing.roster.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterSummaryTest {
    @Test
    fun `starts in created`() {
        val item = newRosterSummary("code-464", "label-279")
        assertEquals(RosterSummaryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRosterSummary("code-464", "label-279")
        try {
            item.moveTo(RosterSummaryStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterSummaryStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRosterSummary("code-464", "label-279")
        item.status = RosterSummaryStatus.RETIRED
        assertFails("move") { item.moveTo(RosterSummaryStatus.CREATED) }
    }
}
