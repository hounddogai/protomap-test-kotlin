package com.acme.transport.freightpay.audit.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalAuditDraftTest {
    @Test
    fun `starts in idle`() {
        val item = newSeasonalAuditDraft("code-973", "label-855")
        assertEquals(SeasonalAuditDraftStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newSeasonalAuditDraft("code-973", "label-855")
        try {
            item.moveTo(SeasonalAuditDraftStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalAuditDraftStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSeasonalAuditDraft("code-973", "label-855")
        item.status = SeasonalAuditDraftStatus.STOPPED
        assertFails("move") { item.moveTo(SeasonalAuditDraftStatus.IDLE) }
    }
}
