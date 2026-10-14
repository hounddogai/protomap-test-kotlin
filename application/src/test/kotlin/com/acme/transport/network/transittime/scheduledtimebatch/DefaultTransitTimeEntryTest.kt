package com.acme.transport.network.transittime.scheduledtimebatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultTransitTimeEntryTest {
    @Test
    fun `starts in new`() {
        val item = newDefaultTransitTimeEntry("code-91", "title-764")
        assertEquals(DefaultTransitTimeEntryStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newDefaultTransitTimeEntry("code-91", "title-764")
        try {
            item.moveTo(DefaultTransitTimeEntryStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultTransitTimeEntryStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefaultTransitTimeEntry("code-91", "title-764")
        item.status = DefaultTransitTimeEntryStatus.FAILED
        assertFails("move") { item.moveTo(DefaultTransitTimeEntryStatus.NEW) }
    }
}
