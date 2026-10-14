package com.acme.transport.crewing.roster.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterSnapshotTest {
    @Test
    fun `starts in planned`() {
        val item = newRosterSnapshot("code-434", "name-755")
        assertEquals(RosterSnapshotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newRosterSnapshot("code-434", "name-755")
        try {
            item.moveTo(RosterSnapshotStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterSnapshotStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRosterSnapshot("code-434", "name-755")
        item.status = RosterSnapshotStatus.CANCELLED
        assertFails("move") { item.moveTo(RosterSnapshotStatus.PLANNED) }
    }
}
