package com.acme.transport.customs.inspection.inspectionrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionAuditTest {
    @Test
    fun `starts in requested`() {
        val item = newInspectionAudit("code-85", "title-16")
        assertEquals(InspectionAuditStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newInspectionAudit("code-85", "title-16")
        try {
            item.moveTo(InspectionAuditStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionAuditStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newInspectionAudit("code-85", "title-16")
        item.status = InspectionAuditStatus.EXPIRED
        assertFails("move") { item.moveTo(InspectionAuditStatus.REQUESTED) }
    }
}
