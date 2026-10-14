package com.acme.transport.freightpay.accessorial.scheduledaccessorialmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledAccessorialDraftTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledAccessorialDraft("code-562", "name-435")
        assertEquals(ScheduledAccessorialDraftStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newScheduledAccessorialDraft("code-562", "name-435")
        try {
            item.moveTo(ScheduledAccessorialDraftStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledAccessorialDraftStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledAccessorialDraft("code-562", "name-435")
        item.status = ScheduledAccessorialDraftStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledAccessorialDraftStatus.NEW) }
    }
}
