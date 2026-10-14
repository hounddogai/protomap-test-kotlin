package com.acme.transport.customs.inspection.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryInspectionAuditTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryInspectionAudit("code-676", "title-441")
        assertEquals(PrimaryInspectionAuditStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newPrimaryInspectionAudit("code-676", "title-441")
        try {
            item.moveTo(PrimaryInspectionAuditStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryInspectionAuditStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryInspectionAudit("code-676", "title-441")
        item.status = PrimaryInspectionAuditStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryInspectionAuditStatus.NEW) }
    }
}
