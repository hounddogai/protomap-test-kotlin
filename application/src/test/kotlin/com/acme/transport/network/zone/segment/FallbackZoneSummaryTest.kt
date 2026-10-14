package com.acme.transport.network.zone.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackZoneSummaryTest {
    @Test
    fun `starts in proposed`() {
        val item = newFallbackZoneSummary("code-648", "title-826")
        assertEquals(FallbackZoneSummaryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newFallbackZoneSummary("code-648", "title-826")
        try {
            item.moveTo(FallbackZoneSummaryStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackZoneSummaryStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFallbackZoneSummary("code-648", "title-826")
        item.status = FallbackZoneSummaryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FallbackZoneSummaryStatus.PROPOSED) }
    }
}
