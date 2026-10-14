package com.acme.transport.network.zone.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredZoneSummaryTest {
    @Test
    fun `starts in new`() {
        val item = newTieredZoneSummary("code-103", "title-154")
        assertEquals(TieredZoneSummaryStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newTieredZoneSummary("code-103", "title-154")
        try {
            item.moveTo(TieredZoneSummaryStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredZoneSummaryStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTieredZoneSummary("code-103", "title-154")
        item.status = TieredZoneSummaryStatus.FAILED
        assertFails("move") { item.moveTo(TieredZoneSummaryStatus.NEW) }
    }
}
