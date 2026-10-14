package com.acme.transport.linehaul.tender.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderDraftTest {
    @Test
    fun `starts in new`() {
        val item = newTenderDraft("code-859", "label-349")
        assertEquals(TenderDraftStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newTenderDraft("code-859", "label-349")
        try {
            item.moveTo(TenderDraftStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderDraftStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTenderDraft("code-859", "label-349")
        item.status = TenderDraftStatus.FAILED
        assertFails("move") { item.moveTo(TenderDraftStatus.NEW) }
    }
}
