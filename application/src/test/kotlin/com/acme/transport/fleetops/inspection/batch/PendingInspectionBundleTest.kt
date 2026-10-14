package com.acme.transport.fleetops.inspection.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingInspectionBundleTest {
    @Test
    fun `starts in created`() {
        val item = newPendingInspectionBundle("code-179", "title-80")
        assertEquals(PendingInspectionBundleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newPendingInspectionBundle("code-179", "title-80")
        try {
            item.moveTo(PendingInspectionBundleStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingInspectionBundleStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPendingInspectionBundle("code-179", "title-80")
        item.status = PendingInspectionBundleStatus.RETIRED
        assertFails("move") { item.moveTo(PendingInspectionBundleStatus.CREATED) }
    }
}
