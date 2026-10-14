package com.acme.transport.customs.inspection.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionDraftTest {
    @Test
    fun `starts in open`() {
        val item = newInspectionDraft("code-966", "name-452")
        assertEquals(InspectionDraftStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newInspectionDraft("code-966", "name-452")
        try {
            item.moveTo(InspectionDraftStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionDraftStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newInspectionDraft("code-966", "name-452")
        item.status = InspectionDraftStatus.ARCHIVED
        assertFails("move") { item.moveTo(InspectionDraftStatus.OPEN) }
    }
}
