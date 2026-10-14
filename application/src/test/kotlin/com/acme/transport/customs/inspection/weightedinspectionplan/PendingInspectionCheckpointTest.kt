package com.acme.transport.customs.inspection.weightedinspectionplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingInspectionCheckpointTest {
    @Test
    fun `starts in created`() {
        val item = newPendingInspectionCheckpoint("code-266", "name-931")
        assertEquals(PendingInspectionCheckpointStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newPendingInspectionCheckpoint("code-266", "name-931")
        try {
            item.moveTo(PendingInspectionCheckpointStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingInspectionCheckpointStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPendingInspectionCheckpoint("code-266", "name-931")
        item.status = PendingInspectionCheckpointStatus.RETIRED
        assertFails("move") { item.moveTo(PendingInspectionCheckpointStatus.CREATED) }
    }
}
