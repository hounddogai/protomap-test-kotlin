package com.acme.transport.fleetops.defect.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectCheckpointTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefectCheckpoint("code-432", "name-748")
        assertEquals(DefectCheckpointStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDefectCheckpoint("code-432", "name-748")
        try {
            item.moveTo(DefectCheckpointStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectCheckpointStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefectCheckpoint("code-432", "name-748")
        item.status = DefectCheckpointStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefectCheckpointStatus.PROPOSED) }
    }
}
