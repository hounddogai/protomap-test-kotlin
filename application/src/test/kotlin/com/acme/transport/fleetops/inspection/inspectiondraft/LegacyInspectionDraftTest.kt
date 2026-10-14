package com.acme.transport.fleetops.inspection.inspectiondraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyInspectionDraftTest {
    @Test
    fun `starts in open`() {
        val item = newLegacyInspectionDraft("code-637", "label-427")
        assertEquals(LegacyInspectionDraftStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newLegacyInspectionDraft("code-637", "label-427")
        try {
            item.moveTo(LegacyInspectionDraftStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyInspectionDraftStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLegacyInspectionDraft("code-637", "label-427")
        item.status = LegacyInspectionDraftStatus.ARCHIVED
        assertFails("move") { item.moveTo(LegacyInspectionDraftStatus.OPEN) }
    }
}
