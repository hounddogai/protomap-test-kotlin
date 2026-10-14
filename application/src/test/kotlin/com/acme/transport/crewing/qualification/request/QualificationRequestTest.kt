package com.acme.transport.crewing.qualification.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationRequestTest {
    @Test
    fun `starts in open`() {
        val item = newQualificationRequest("code-635", "label-278")
        assertEquals(QualificationRequestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newQualificationRequest("code-635", "label-278")
        try {
            item.moveTo(QualificationRequestStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationRequestStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newQualificationRequest("code-635", "label-278")
        item.status = QualificationRequestStatus.ARCHIVED
        assertFails("move") { item.moveTo(QualificationRequestStatus.OPEN) }
    }
}
