package com.acme.transport.lastmile.parcel.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryParcelDraftTest {
    @Test
    fun `starts in draft`() {
        val item = newPrimaryParcelDraft("code-594", "label-332")
        assertEquals(PrimaryParcelDraftStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newPrimaryParcelDraft("code-594", "label-332")
        try {
            item.moveTo(PrimaryParcelDraftStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryParcelDraftStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPrimaryParcelDraft("code-594", "label-332")
        item.status = PrimaryParcelDraftStatus.CLOSED
        assertFails("move") { item.moveTo(PrimaryParcelDraftStatus.DRAFT) }
    }
}
