package com.acme.transport.crewing.qualification.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationTargetTest {
    @Test
    fun `starts in idle`() {
        val item = newQualificationTarget("code-798", "title-724")
        assertEquals(QualificationTargetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newQualificationTarget("code-798", "title-724")
        try {
            item.moveTo(QualificationTargetStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationTargetStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newQualificationTarget("code-798", "title-724")
        item.status = QualificationTargetStatus.STOPPED
        assertFails("move") { item.moveTo(QualificationTargetStatus.IDLE) }
    }
}
