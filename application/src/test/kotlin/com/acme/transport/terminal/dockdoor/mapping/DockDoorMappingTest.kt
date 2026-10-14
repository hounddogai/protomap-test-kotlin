package com.acme.transport.terminal.dockdoor.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorMappingTest {
    @Test
    fun `starts in draft`() {
        val item = newDockDoorMapping("code-439", "name-212")
        assertEquals(DockDoorMappingStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newDockDoorMapping("code-439", "name-212")
        try {
            item.moveTo(DockDoorMappingStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorMappingStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDockDoorMapping("code-439", "name-212")
        item.status = DockDoorMappingStatus.CLOSED
        assertFails("move") { item.moveTo(DockDoorMappingStatus.DRAFT) }
    }
}
