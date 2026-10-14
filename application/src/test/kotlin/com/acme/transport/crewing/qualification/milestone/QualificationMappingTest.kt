package com.acme.transport.crewing.qualification.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationMappingTest {
    @Test
    fun `starts in open`() {
        val item = newQualificationMapping("code-335", "name-195")
        assertEquals(QualificationMappingStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newQualificationMapping("code-335", "name-195")
        try {
            item.moveTo(QualificationMappingStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationMappingStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newQualificationMapping("code-335", "name-195")
        item.status = QualificationMappingStatus.ARCHIVED
        assertFails("move") { item.moveTo(QualificationMappingStatus.OPEN) }
    }
}
