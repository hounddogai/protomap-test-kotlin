package com.acme.transport.network.zone.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneSummaryTest {
    @Test
    fun `starts in draft`() {
        val item = newZoneSummary("code-98", "title-9")
        assertEquals(ZoneSummaryStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newZoneSummary("code-98", "title-9")
        try {
            item.moveTo(ZoneSummaryStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneSummaryStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newZoneSummary("code-98", "title-9")
        item.status = ZoneSummaryStatus.CLOSED
        assertFails("move") { item.moveTo(ZoneSummaryStatus.DRAFT) }
    }
}
