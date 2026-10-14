package com.acme.transport.fleetops.inspection.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionCheckpointTest {
    @Test
    fun `starts in proposed`() {
        val item = newInspectionCheckpoint("code-887", "title-450")
        assertEquals(InspectionCheckpointStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newInspectionCheckpoint("code-887", "title-450")
        try {
            item.moveTo(InspectionCheckpointStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionCheckpointStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newInspectionCheckpoint("code-887", "title-450")
        item.status = InspectionCheckpointStatus.WITHDRAWN
        assertFails("move") { item.moveTo(InspectionCheckpointStatus.PROPOSED) }
    }
}
