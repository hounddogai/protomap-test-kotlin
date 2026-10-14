package com.acme.transport.fleetops.defect.defectcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultDefectCheckpointTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultDefectCheckpoint("code-29", "title-642")
        assertEquals(DefaultDefectCheckpointStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDefaultDefectCheckpoint("code-29", "title-642")
        try {
            item.moveTo(DefaultDefectCheckpointStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultDefectCheckpointStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultDefectCheckpoint("code-29", "title-642")
        item.status = DefaultDefectCheckpointStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultDefectCheckpointStatus.IDLE) }
    }
}
