package com.acme.transport.network.zone.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneDraftTest {
    @Test
    fun `starts in planned`() {
        val item = newZoneDraft("code-802", "label-662")
        assertEquals(ZoneDraftStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newZoneDraft("code-802", "label-662")
        try {
            item.moveTo(ZoneDraftStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneDraftStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newZoneDraft("code-802", "label-662")
        item.status = ZoneDraftStatus.CANCELLED
        assertFails("move") { item.moveTo(ZoneDraftStatus.PLANNED) }
    }
}
