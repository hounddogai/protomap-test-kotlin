package com.acme.transport.network.servicearea.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingServiceAreaAssignmentTest {
    @Test
    fun `starts in open`() {
        val item = newPendingServiceAreaAssignment("code-597", "title-420")
        assertEquals(PendingServiceAreaAssignmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newPendingServiceAreaAssignment("code-597", "title-420")
        try {
            item.moveTo(PendingServiceAreaAssignmentStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingServiceAreaAssignmentStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPendingServiceAreaAssignment("code-597", "title-420")
        item.status = PendingServiceAreaAssignmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(PendingServiceAreaAssignmentStatus.OPEN) }
    }
}
