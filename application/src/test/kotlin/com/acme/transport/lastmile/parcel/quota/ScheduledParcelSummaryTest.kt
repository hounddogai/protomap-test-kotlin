package com.acme.transport.lastmile.parcel.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledParcelSummaryTest {
    @Test
    fun `starts in proposed`() {
        val item = newScheduledParcelSummary("code-313", "label-520")
        assertEquals(ScheduledParcelSummaryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newScheduledParcelSummary("code-313", "label-520")
        try {
            item.moveTo(ScheduledParcelSummaryStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledParcelSummaryStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newScheduledParcelSummary("code-313", "label-520")
        item.status = ScheduledParcelSummaryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ScheduledParcelSummaryStatus.PROPOSED) }
    }
}
