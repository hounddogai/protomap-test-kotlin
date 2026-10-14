package com.acme.transport.fleetops.defect.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectDraftTest {
    @Test
    fun `starts in draft`() {
        val item = newDefectDraft("code-506", "label-290")
        assertEquals(DefectDraftStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newDefectDraft("code-506", "label-290")
        try {
            item.moveTo(DefectDraftStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectDraftStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDefectDraft("code-506", "label-290")
        item.status = DefectDraftStatus.CLOSED
        assertFails("move") { item.moveTo(DefectDraftStatus.DRAFT) }
    }
}
