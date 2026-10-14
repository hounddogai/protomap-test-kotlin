package com.acme.transport.crewing.qualification.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationDigestTest {
    @Test
    fun `starts in new`() {
        val item = newQualificationDigest("code-805", "title-899")
        assertEquals(QualificationDigestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newQualificationDigest("code-805", "title-899")
        try {
            item.moveTo(QualificationDigestStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationDigestStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newQualificationDigest("code-805", "title-899")
        item.status = QualificationDigestStatus.FAILED
        assertFails("move") { item.moveTo(QualificationDigestStatus.NEW) }
    }
}
