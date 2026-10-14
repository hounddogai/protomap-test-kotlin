package com.acme.transport.crewing.qualification.qualificationdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationEntryTest {
    @Test
    fun `starts in open`() {
        val item = newQualificationEntry("code-988", "label-209")
        assertEquals(QualificationEntryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newQualificationEntry("code-988", "label-209")
        try {
            item.moveTo(QualificationEntryStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationEntryStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newQualificationEntry("code-988", "label-209")
        item.status = QualificationEntryStatus.ARCHIVED
        assertFails("move") { item.moveTo(QualificationEntryStatus.OPEN) }
    }
}
