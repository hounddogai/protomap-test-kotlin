package com.acme.transport.crewing.availability.primaryavailabilitytier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualAvailabilityDraftTest {
    @Test
    fun `starts in draft`() {
        val item = newManualAvailabilityDraft("code-562", "title-871")
        assertEquals(ManualAvailabilityDraftStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newManualAvailabilityDraft("code-562", "title-871")
        try {
            item.moveTo(ManualAvailabilityDraftStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualAvailabilityDraftStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualAvailabilityDraft("code-562", "title-871")
        item.status = ManualAvailabilityDraftStatus.CLOSED
        assertFails("move") { item.moveTo(ManualAvailabilityDraftStatus.DRAFT) }
    }
}
