package com.acme.transport.fleetops.defect.defectrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectQuotaTest {
    @Test
    fun `starts in idle`() {
        val item = newDefectQuota("code-926", "title-848")
        assertEquals(DefectQuotaStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newDefectQuota("code-926", "title-848")
        try {
            item.moveTo(DefectQuotaStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectQuotaStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefectQuota("code-926", "title-848")
        item.status = DefectQuotaStatus.STOPPED
        assertFails("move") { item.moveTo(DefectQuotaStatus.IDLE) }
    }
}
