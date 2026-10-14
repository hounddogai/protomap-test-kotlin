package com.acme.transport.fleetops.inspection.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionDraftTest {
    @Test
    fun `starts in draft`() {
        val item = newInspectionDraft("code-647", "name-330")
        assertEquals(InspectionDraftStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newInspectionDraft("code-647", "name-330")
        try {
            item.moveTo(InspectionDraftStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionDraftStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newInspectionDraft("code-647", "name-330")
        item.status = InspectionDraftStatus.CLOSED
        assertFails("move") { item.moveTo(InspectionDraftStatus.DRAFT) }
    }
}
