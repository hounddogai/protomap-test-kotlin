package com.acme.transport.fleetops.inspection.inspectiondraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingInspectionRequestTest {
    @Test
    fun `starts in proposed`() {
        val item = newPendingInspectionRequest("code-964", "name-711")
        assertEquals(PendingInspectionRequestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newPendingInspectionRequest("code-964", "name-711")
        try {
            item.moveTo(PendingInspectionRequestStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingInspectionRequestStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPendingInspectionRequest("code-964", "name-711")
        item.status = PendingInspectionRequestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PendingInspectionRequestStatus.PROPOSED) }
    }
}
