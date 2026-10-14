package com.acme.transport.crewing.qualification.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationCheckpointTest {
    @Test
    fun `starts in requested`() {
        val item = newQualificationCheckpoint("code-48", "title-228")
        assertEquals(QualificationCheckpointStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newQualificationCheckpoint("code-48", "title-228")
        try {
            item.moveTo(QualificationCheckpointStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationCheckpointStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newQualificationCheckpoint("code-48", "title-228")
        item.status = QualificationCheckpointStatus.EXPIRED
        assertFails("move") { item.moveTo(QualificationCheckpointStatus.REQUESTED) }
    }
}
