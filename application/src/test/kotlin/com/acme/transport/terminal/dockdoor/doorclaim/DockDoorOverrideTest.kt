package com.acme.transport.terminal.dockdoor.doorclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorOverrideTest {
    @Test
    fun `starts in created`() {
        val item = newDockDoorOverride("code-182", "label-839")
        assertEquals(DockDoorOverrideStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newDockDoorOverride("code-182", "label-839")
        try {
            item.moveTo(DockDoorOverrideStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorOverrideStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDockDoorOverride("code-182", "label-839")
        item.status = DockDoorOverrideStatus.RETIRED
        assertFails("move") { item.moveTo(DockDoorOverrideStatus.CREATED) }
    }
}
