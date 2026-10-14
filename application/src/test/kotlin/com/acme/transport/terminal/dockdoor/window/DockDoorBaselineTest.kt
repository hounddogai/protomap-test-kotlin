package com.acme.transport.terminal.dockdoor.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorBaselineTest {
    @Test
    fun `starts in open`() {
        val item = newDockDoorBaseline("code-557", "label-957")
        assertEquals(DockDoorBaselineStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newDockDoorBaseline("code-557", "label-957")
        try {
            item.moveTo(DockDoorBaselineStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorBaselineStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDockDoorBaseline("code-557", "label-957")
        item.status = DockDoorBaselineStatus.ARCHIVED
        assertFails("move") { item.moveTo(DockDoorBaselineStatus.OPEN) }
    }
}
