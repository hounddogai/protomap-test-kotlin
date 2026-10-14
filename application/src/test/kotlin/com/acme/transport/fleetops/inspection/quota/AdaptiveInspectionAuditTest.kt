package com.acme.transport.fleetops.inspection.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveInspectionAuditTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveInspectionAudit("code-704", "name-893")
        assertEquals(AdaptiveInspectionAuditStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newAdaptiveInspectionAudit("code-704", "name-893")
        try {
            item.moveTo(AdaptiveInspectionAuditStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveInspectionAuditStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveInspectionAudit("code-704", "name-893")
        item.status = AdaptiveInspectionAuditStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveInspectionAuditStatus.PLANNED) }
    }
}
