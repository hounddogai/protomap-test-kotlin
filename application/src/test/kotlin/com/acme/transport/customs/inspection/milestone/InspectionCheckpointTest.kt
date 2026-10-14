package com.acme.transport.customs.inspection.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionCheckpointTest {
    @Test
    fun `starts in draft`() {
        val item = newInspectionCheckpoint("code-259", "name-865")
        assertEquals(InspectionCheckpointStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newInspectionCheckpoint("code-259", "name-865")
        try {
            item.moveTo(InspectionCheckpointStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionCheckpointStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newInspectionCheckpoint("code-259", "name-865")
        item.status = InspectionCheckpointStatus.CLOSED
        assertFails("move") { item.moveTo(InspectionCheckpointStatus.DRAFT) }
    }
}
