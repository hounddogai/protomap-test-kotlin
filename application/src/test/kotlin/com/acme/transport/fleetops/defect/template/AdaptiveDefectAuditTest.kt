package com.acme.transport.fleetops.defect.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveDefectAuditTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveDefectAudit("code-979", "label-185")
        assertEquals(AdaptiveDefectAuditStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newAdaptiveDefectAudit("code-979", "label-185")
        try {
            item.moveTo(AdaptiveDefectAuditStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveDefectAuditStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveDefectAudit("code-979", "label-185")
        item.status = AdaptiveDefectAuditStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveDefectAuditStatus.IDLE) }
    }
}
