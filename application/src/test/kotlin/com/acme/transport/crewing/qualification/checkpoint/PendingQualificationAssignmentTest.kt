package com.acme.transport.crewing.qualification.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingQualificationAssignmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newPendingQualificationAssignment("code-906", "label-214")
        assertEquals(PendingQualificationAssignmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newPendingQualificationAssignment("code-906", "label-214")
        try {
            item.moveTo(PendingQualificationAssignmentStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingQualificationAssignmentStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPendingQualificationAssignment("code-906", "label-214")
        item.status = PendingQualificationAssignmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PendingQualificationAssignmentStatus.PROPOSED) }
    }
}
