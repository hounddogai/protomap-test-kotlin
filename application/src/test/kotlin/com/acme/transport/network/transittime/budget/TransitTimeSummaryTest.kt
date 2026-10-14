package com.acme.transport.network.transittime.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeSummaryTest {
    @Test
    fun `starts in proposed`() {
        val item = newTransitTimeSummary("code-465", "name-907")
        assertEquals(TransitTimeSummaryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTransitTimeSummary("code-465", "name-907")
        try {
            item.moveTo(TransitTimeSummaryStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeSummaryStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTransitTimeSummary("code-465", "name-907")
        item.status = TransitTimeSummaryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TransitTimeSummaryStatus.PROPOSED) }
    }
}
