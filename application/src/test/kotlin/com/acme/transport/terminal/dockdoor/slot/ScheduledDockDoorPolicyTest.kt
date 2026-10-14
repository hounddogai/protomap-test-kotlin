package com.acme.transport.terminal.dockdoor.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledDockDoorPolicyTest {
    @Test
    fun `starts in proposed`() {
        val item = newScheduledDockDoorPolicy("code-599", "title-650")
        assertEquals(ScheduledDockDoorPolicyStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newScheduledDockDoorPolicy("code-599", "title-650")
        try {
            item.moveTo(ScheduledDockDoorPolicyStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledDockDoorPolicyStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newScheduledDockDoorPolicy("code-599", "title-650")
        item.status = ScheduledDockDoorPolicyStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ScheduledDockDoorPolicyStatus.PROPOSED) }
    }
}
