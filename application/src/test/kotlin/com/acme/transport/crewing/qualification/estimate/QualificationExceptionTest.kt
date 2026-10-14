package com.acme.transport.crewing.qualification.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationExceptionTest {
    @Test
    fun `starts in requested`() {
        val item = newQualificationException("code-361", "title-73")
        assertEquals(QualificationExceptionStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newQualificationException("code-361", "title-73")
        try {
            item.moveTo(QualificationExceptionStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationExceptionStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newQualificationException("code-361", "title-73")
        item.status = QualificationExceptionStatus.EXPIRED
        assertFails("move") { item.moveTo(QualificationExceptionStatus.REQUESTED) }
    }
}
