package com.acme.transport.customs.inspection.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalInspectionAuditTest {
    @Test
    fun `starts in open`() {
        val item = newRegionalInspectionAudit("code-751", "name-379")
        assertEquals(RegionalInspectionAuditStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newRegionalInspectionAudit("code-751", "name-379")
        try {
            item.moveTo(RegionalInspectionAuditStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalInspectionAuditStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRegionalInspectionAudit("code-751", "name-379")
        item.status = RegionalInspectionAuditStatus.ARCHIVED
        assertFails("move") { item.moveTo(RegionalInspectionAuditStatus.OPEN) }
    }
}
