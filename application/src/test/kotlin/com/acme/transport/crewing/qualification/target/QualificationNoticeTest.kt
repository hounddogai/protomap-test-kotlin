package com.acme.transport.crewing.qualification.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationNoticeTest {
    @Test
    fun `starts in planned`() {
        val item = newQualificationNotice("code-404", "label-15")
        assertEquals(QualificationNoticeStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newQualificationNotice("code-404", "label-15")
        try {
            item.moveTo(QualificationNoticeStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationNoticeStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newQualificationNotice("code-404", "label-15")
        item.status = QualificationNoticeStatus.CANCELLED
        assertFails("move") { item.moveTo(QualificationNoticeStatus.PLANNED) }
    }
}
