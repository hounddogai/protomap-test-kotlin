package com.acme.transport.fleetops.inspection.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedInspectionCheckpointTest {
    @Test
    fun `starts in planned`() {
        val item = newCappedInspectionCheckpoint("code-490", "name-391")
        assertEquals(CappedInspectionCheckpointStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newCappedInspectionCheckpoint("code-490", "name-391")
        try {
            item.moveTo(CappedInspectionCheckpointStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedInspectionCheckpointStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCappedInspectionCheckpoint("code-490", "name-391")
        item.status = CappedInspectionCheckpointStatus.CANCELLED
        assertFails("move") { item.moveTo(CappedInspectionCheckpointStatus.PLANNED) }
    }
}
