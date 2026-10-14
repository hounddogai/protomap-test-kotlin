package com.acme.transport.terminal.dockdoor.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDockDoorPolicyTest {
    @Test
    fun `starts in open`() {
        val item = newStrictDockDoorPolicy("code-31", "name-61")
        assertEquals(StrictDockDoorPolicyStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newStrictDockDoorPolicy("code-31", "name-61")
        try {
            item.moveTo(StrictDockDoorPolicyStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDockDoorPolicyStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictDockDoorPolicy("code-31", "name-61")
        item.status = StrictDockDoorPolicyStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictDockDoorPolicyStatus.OPEN) }
    }
}
