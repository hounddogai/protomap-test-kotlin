package com.acme.transport.terminal.crossdock.dockslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledCrossDockSlotTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledCrossDockSlot("code-200", "name-571")
        assertEquals(ScheduledCrossDockSlotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newScheduledCrossDockSlot("code-200", "name-571")
        try {
            item.moveTo(ScheduledCrossDockSlotStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledCrossDockSlotStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledCrossDockSlot("code-200", "name-571")
        item.status = ScheduledCrossDockSlotStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledCrossDockSlotStatus.CREATED) }
    }
}
