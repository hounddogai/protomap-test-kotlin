package com.acme.transport.terminal.dockdoor.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorPreferenceTest {
    @Test
    fun `starts in requested`() {
        val item = newDockDoorPreference("code-219", "label-793")
        assertEquals(DockDoorPreferenceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newDockDoorPreference("code-219", "label-793")
        try {
            item.moveTo(DockDoorPreferenceStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorPreferenceStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDockDoorPreference("code-219", "label-793")
        item.status = DockDoorPreferenceStatus.EXPIRED
        assertFails("move") { item.moveTo(DockDoorPreferenceStatus.REQUESTED) }
    }
}
