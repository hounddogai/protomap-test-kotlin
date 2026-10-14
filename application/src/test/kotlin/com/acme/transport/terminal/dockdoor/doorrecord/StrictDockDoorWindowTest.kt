package com.acme.transport.terminal.dockdoor.doorrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDockDoorWindowTest {
    @Test
    fun `starts in requested`() {
        val item = newStrictDockDoorWindow("code-650", "name-834")
        assertEquals(StrictDockDoorWindowStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newStrictDockDoorWindow("code-650", "name-834")
        try {
            item.moveTo(StrictDockDoorWindowStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDockDoorWindowStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newStrictDockDoorWindow("code-650", "name-834")
        item.status = StrictDockDoorWindowStatus.EXPIRED
        assertFails("move") { item.moveTo(StrictDockDoorWindowStatus.REQUESTED) }
    }
}
