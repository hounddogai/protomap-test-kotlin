package com.acme.transport.fleetops.inspection.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionAuditTest {
    @Test
    fun `starts in requested`() {
        val item = newInspectionAudit("code-209", "title-201")
        assertEquals(InspectionAuditStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newInspectionAudit("code-209", "title-201")
        try {
            item.moveTo(InspectionAuditStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionAuditStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newInspectionAudit("code-209", "title-201")
        item.status = InspectionAuditStatus.EXPIRED
        assertFails("move") { item.moveTo(InspectionAuditStatus.REQUESTED) }
    }
}
