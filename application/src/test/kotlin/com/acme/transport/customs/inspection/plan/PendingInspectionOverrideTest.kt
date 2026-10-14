package com.acme.transport.customs.inspection.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingInspectionOverrideTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingInspectionOverride("code-154", "title-893")
        assertEquals(PendingInspectionOverrideStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newPendingInspectionOverride("code-154", "title-893")
        try {
            item.moveTo(PendingInspectionOverrideStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingInspectionOverrideStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingInspectionOverride("code-154", "title-893")
        item.status = PendingInspectionOverrideStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingInspectionOverrideStatus.PLANNED) }
    }
}
