package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalDockDoorSummaryTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalDockDoorSummary("code-697", "label-541")
        assertEquals(GlobalDockDoorSummaryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newGlobalDockDoorSummary("code-697", "label-541")
        try {
            item.moveTo(GlobalDockDoorSummaryStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalDockDoorSummaryStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalDockDoorSummary("code-697", "label-541")
        item.status = GlobalDockDoorSummaryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalDockDoorSummaryStatus.PROPOSED) }
    }
}
