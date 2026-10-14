package com.acme.transport.network.servicearea.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingServiceAreaEntryTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingServiceAreaEntry("code-146", "title-22")
        assertEquals(PendingServiceAreaEntryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPendingServiceAreaEntry("code-146", "title-22")
        try {
            item.moveTo(PendingServiceAreaEntryStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingServiceAreaEntryStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingServiceAreaEntry("code-146", "title-22")
        item.status = PendingServiceAreaEntryStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingServiceAreaEntryStatus.REQUESTED) }
    }
}
