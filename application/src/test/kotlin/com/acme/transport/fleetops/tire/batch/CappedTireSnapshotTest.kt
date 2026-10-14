package com.acme.transport.fleetops.tire.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedTireSnapshotTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedTireSnapshot("code-720", "name-812")
        assertEquals(CappedTireSnapshotStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newCappedTireSnapshot("code-720", "name-812")
        try {
            item.moveTo(CappedTireSnapshotStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedTireSnapshotStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedTireSnapshot("code-720", "name-812")
        item.status = CappedTireSnapshotStatus.CLOSED
        assertFails("move") { item.moveTo(CappedTireSnapshotStatus.DRAFT) }
    }
}
