package com.acme.transport.customs.inspection.inspectionoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveInspectionDraftTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveInspectionDraft("code-722", "name-575")
        assertEquals(AdaptiveInspectionDraftStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newAdaptiveInspectionDraft("code-722", "name-575")
        try {
            item.moveTo(AdaptiveInspectionDraftStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveInspectionDraftStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveInspectionDraft("code-722", "name-575")
        item.status = AdaptiveInspectionDraftStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveInspectionDraftStatus.DRAFT) }
    }
}
