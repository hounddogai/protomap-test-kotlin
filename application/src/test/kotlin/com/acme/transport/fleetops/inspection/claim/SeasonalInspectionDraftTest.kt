package com.acme.transport.fleetops.inspection.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalInspectionDraftTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalInspectionDraft("code-486", "title-711")
        assertEquals(SeasonalInspectionDraftStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newSeasonalInspectionDraft("code-486", "title-711")
        try {
            item.moveTo(SeasonalInspectionDraftStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalInspectionDraftStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalInspectionDraft("code-486", "title-711")
        item.status = SeasonalInspectionDraftStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalInspectionDraftStatus.REQUESTED) }
    }
}
