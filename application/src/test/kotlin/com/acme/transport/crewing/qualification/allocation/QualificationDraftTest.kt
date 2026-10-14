package com.acme.transport.crewing.qualification.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationDraftTest {
    @Test
    fun `starts in proposed`() {
        val item = newQualificationDraft("code-891", "label-493")
        assertEquals(QualificationDraftStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newQualificationDraft("code-891", "label-493")
        try {
            item.moveTo(QualificationDraftStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationDraftStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newQualificationDraft("code-891", "label-493")
        item.status = QualificationDraftStatus.WITHDRAWN
        assertFails("move") { item.moveTo(QualificationDraftStatus.PROPOSED) }
    }
}
