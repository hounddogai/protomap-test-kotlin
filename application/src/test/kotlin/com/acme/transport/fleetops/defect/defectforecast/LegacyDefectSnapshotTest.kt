package com.acme.transport.fleetops.defect.defectforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyDefectSnapshotTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyDefectSnapshot("code-413", "name-823")
        assertEquals(LegacyDefectSnapshotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLegacyDefectSnapshot("code-413", "name-823")
        try {
            item.moveTo(LegacyDefectSnapshotStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyDefectSnapshotStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyDefectSnapshot("code-413", "name-823")
        item.status = LegacyDefectSnapshotStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyDefectSnapshotStatus.PLANNED) }
    }
}
