package com.acme.transport.fleetops.defect.defectrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectAuditTest {
    @Test
    fun `starts in planned`() {
        val item = newDefectAudit("code-703", "name-861")
        assertEquals(DefectAuditStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newDefectAudit("code-703", "name-861")
        try {
            item.moveTo(DefectAuditStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectAuditStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDefectAudit("code-703", "name-861")
        item.status = DefectAuditStatus.CANCELLED
        assertFails("move") { item.moveTo(DefectAuditStatus.PLANNED) }
    }
}
