package com.acme.transport.linehaul.capacity.capacitysnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryCapacitySnapshotTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryCapacitySnapshot("code-742", "name-471")
        assertEquals(PrimaryCapacitySnapshotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newPrimaryCapacitySnapshot("code-742", "name-471")
        try {
            item.moveTo(PrimaryCapacitySnapshotStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryCapacitySnapshotStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryCapacitySnapshot("code-742", "name-471")
        item.status = PrimaryCapacitySnapshotStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryCapacitySnapshotStatus.OPEN) }
    }
}
