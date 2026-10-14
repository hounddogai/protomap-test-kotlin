package com.acme.transport.network.zone.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictZoneSummaryTest {
    @Test
    fun `starts in proposed`() {
        val item = newStrictZoneSummary("code-47", "label-44")
        assertEquals(StrictZoneSummaryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newStrictZoneSummary("code-47", "label-44")
        try {
            item.moveTo(StrictZoneSummaryStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictZoneSummaryStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newStrictZoneSummary("code-47", "label-44")
        item.status = StrictZoneSummaryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(StrictZoneSummaryStatus.PROPOSED) }
    }
}
