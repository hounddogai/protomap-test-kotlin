package com.acme.transport.linehaul.tender.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryTenderDraftTest {
    @Test
    fun `starts in idle`() {
        val item = newPrimaryTenderDraft("code-688", "title-649")
        assertEquals(PrimaryTenderDraftStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newPrimaryTenderDraft("code-688", "title-649")
        try {
            item.moveTo(PrimaryTenderDraftStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryTenderDraftStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPrimaryTenderDraft("code-688", "title-649")
        item.status = PrimaryTenderDraftStatus.STOPPED
        assertFails("move") { item.moveTo(PrimaryTenderDraftStatus.IDLE) }
    }
}
