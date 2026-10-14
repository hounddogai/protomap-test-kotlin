package com.acme.transport.terminal.dockdoor.dooragreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorPolicyTest {
    @Test
    fun `starts in requested`() {
        val item = newDockDoorPolicy("code-194", "name-200")
        assertEquals(DockDoorPolicyStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDockDoorPolicy("code-194", "name-200")
        try {
            item.moveTo(DockDoorPolicyStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorPolicyStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDockDoorPolicy("code-194", "name-200")
        item.status = DockDoorPolicyStatus.EXPIRED
        assertFails("move") { item.moveTo(DockDoorPolicyStatus.REQUESTED) }
    }
}
