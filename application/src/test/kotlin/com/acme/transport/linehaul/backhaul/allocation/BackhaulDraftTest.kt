package com.acme.transport.linehaul.backhaul.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulDraftTest {
    @Test
    fun `starts in planned`() {
        val item = newBackhaulDraft("code-931", "label-913")
        assertEquals(BackhaulDraftStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newBackhaulDraft("code-931", "label-913")
        try {
            item.moveTo(BackhaulDraftStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulDraftStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBackhaulDraft("code-931", "label-913")
        item.status = BackhaulDraftStatus.CANCELLED
        assertFails("move") { item.moveTo(BackhaulDraftStatus.PLANNED) }
    }
}
