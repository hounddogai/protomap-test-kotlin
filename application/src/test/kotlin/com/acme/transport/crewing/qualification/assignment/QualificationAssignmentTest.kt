package com.acme.transport.crewing.qualification.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationAssignmentTest {
    @Test
    fun `starts in open`() {
        val item = newQualificationAssignment("code-709", "title-48")
        assertEquals(QualificationAssignmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newQualificationAssignment("code-709", "title-48")
        try {
            item.moveTo(QualificationAssignmentStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationAssignmentStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newQualificationAssignment("code-709", "title-48")
        item.status = QualificationAssignmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(QualificationAssignmentStatus.OPEN) }
    }
}
