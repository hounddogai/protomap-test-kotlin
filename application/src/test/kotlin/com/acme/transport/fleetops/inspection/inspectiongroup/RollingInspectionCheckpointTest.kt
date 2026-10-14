package com.acme.transport.fleetops.inspection.inspectiongroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingInspectionCheckpointTest {
    @Test
    fun `starts in requested`() {
        val item = newRollingInspectionCheckpoint("code-656", "name-299")
        assertEquals(RollingInspectionCheckpointStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRollingInspectionCheckpoint("code-656", "name-299")
        try {
            item.moveTo(RollingInspectionCheckpointStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingInspectionCheckpointStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRollingInspectionCheckpoint("code-656", "name-299")
        item.status = RollingInspectionCheckpointStatus.EXPIRED
        assertFails("move") { item.moveTo(RollingInspectionCheckpointStatus.REQUESTED) }
    }
}
