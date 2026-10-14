package com.acme.transport.terminal.dockdoor.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorSummaryTest {
    @Test
    fun `starts in requested`() {
        val item = newDockDoorSummary("code-71", "name-638")
        assertEquals(DockDoorSummaryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newDockDoorSummary("code-71", "name-638")
        try {
            item.moveTo(DockDoorSummaryStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorSummaryStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDockDoorSummary("code-71", "name-638")
        item.status = DockDoorSummaryStatus.EXPIRED
        assertFails("move") { item.moveTo(DockDoorSummaryStatus.REQUESTED) }
    }
}
