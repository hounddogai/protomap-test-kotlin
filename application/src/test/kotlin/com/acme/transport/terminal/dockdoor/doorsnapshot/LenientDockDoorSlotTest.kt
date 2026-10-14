package com.acme.transport.terminal.dockdoor.doorsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDockDoorSlotTest {
    @Test
    fun `starts in proposed`() {
        val item = newLenientDockDoorSlot("code-699", "label-377")
        assertEquals(LenientDockDoorSlotStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newLenientDockDoorSlot("code-699", "label-377")
        try {
            item.moveTo(LenientDockDoorSlotStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDockDoorSlotStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLenientDockDoorSlot("code-699", "label-377")
        item.status = LenientDockDoorSlotStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LenientDockDoorSlotStatus.PROPOSED) }
    }
}
