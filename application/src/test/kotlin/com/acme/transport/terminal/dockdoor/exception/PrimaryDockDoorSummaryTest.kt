package com.acme.transport.terminal.dockdoor.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryDockDoorSummaryTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryDockDoorSummary("code-31", "title-795")
        assertEquals(PrimaryDockDoorSummaryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newPrimaryDockDoorSummary("code-31", "title-795")
        try {
            item.moveTo(PrimaryDockDoorSummaryStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryDockDoorSummaryStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryDockDoorSummary("code-31", "title-795")
        item.status = PrimaryDockDoorSummaryStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryDockDoorSummaryStatus.REQUESTED) }
    }
}
