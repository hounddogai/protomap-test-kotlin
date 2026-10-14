package com.acme.transport.customs.inspection.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveInspectionAuditTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveInspectionAudit("code-784", "name-615")
        assertEquals(AdaptiveInspectionAuditStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newAdaptiveInspectionAudit("code-784", "name-615")
        try {
            item.moveTo(AdaptiveInspectionAuditStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveInspectionAuditStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveInspectionAudit("code-784", "name-615")
        item.status = AdaptiveInspectionAuditStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveInspectionAuditStatus.IDLE) }
    }
}
