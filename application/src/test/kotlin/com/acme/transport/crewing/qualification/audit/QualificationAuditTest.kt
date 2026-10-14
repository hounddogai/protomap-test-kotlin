package com.acme.transport.crewing.qualification.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationAuditTest {
    @Test
    fun `starts in draft`() {
        val item = newQualificationAudit("code-959", "label-356")
        assertEquals(QualificationAuditStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newQualificationAudit("code-959", "label-356")
        try {
            item.moveTo(QualificationAuditStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationAuditStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newQualificationAudit("code-959", "label-356")
        item.status = QualificationAuditStatus.CLOSED
        assertFails("move") { item.moveTo(QualificationAuditStatus.DRAFT) }
    }
}
