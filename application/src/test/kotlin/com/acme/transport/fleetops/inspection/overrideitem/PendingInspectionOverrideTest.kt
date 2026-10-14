package com.acme.transport.fleetops.inspection.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingInspectionOverrideTest {
    @Test
    fun `starts in open`() {
        val item = newPendingInspectionOverride("code-55", "title-489")
        assertEquals(PendingInspectionOverrideStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newPendingInspectionOverride("code-55", "title-489")
        try {
            item.moveTo(PendingInspectionOverrideStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingInspectionOverrideStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPendingInspectionOverride("code-55", "title-489")
        item.status = PendingInspectionOverrideStatus.ARCHIVED
        assertFails("move") { item.moveTo(PendingInspectionOverrideStatus.OPEN) }
    }
}
