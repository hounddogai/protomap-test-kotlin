package com.acme.transport.crewing.availability.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilitySnapshotTest {
    @Test
    fun `starts in created`() {
        val item = newAvailabilitySnapshot("code-515", "name-820")
        assertEquals(AvailabilitySnapshotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newAvailabilitySnapshot("code-515", "name-820")
        try {
            item.moveTo(AvailabilitySnapshotStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilitySnapshotStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAvailabilitySnapshot("code-515", "name-820")
        item.status = AvailabilitySnapshotStatus.RETIRED
        assertFails("move") { item.moveTo(AvailabilitySnapshotStatus.CREATED) }
    }
}
