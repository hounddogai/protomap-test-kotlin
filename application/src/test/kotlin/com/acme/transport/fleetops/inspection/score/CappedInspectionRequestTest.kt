package com.acme.transport.fleetops.inspection.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedInspectionRequestTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedInspectionRequest("code-305", "label-933")
        assertEquals(CappedInspectionRequestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newCappedInspectionRequest("code-305", "label-933")
        try {
            item.moveTo(CappedInspectionRequestStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedInspectionRequestStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedInspectionRequest("code-305", "label-933")
        item.status = CappedInspectionRequestStatus.CLOSED
        assertFails("move") { item.moveTo(CappedInspectionRequestStatus.DRAFT) }
    }
}
