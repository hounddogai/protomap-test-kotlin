package com.acme.transport.safety.incident.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentSnapshotTest {
    @Test
    fun `starts in open`() {
        val item = newIncidentSnapshot("code-314", "title-361")
        assertEquals(IncidentSnapshotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newIncidentSnapshot("code-314", "title-361")
        try {
            item.moveTo(IncidentSnapshotStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentSnapshotStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newIncidentSnapshot("code-314", "title-361")
        item.status = IncidentSnapshotStatus.ARCHIVED
        assertFails("move") { item.moveTo(IncidentSnapshotStatus.OPEN) }
    }
}
