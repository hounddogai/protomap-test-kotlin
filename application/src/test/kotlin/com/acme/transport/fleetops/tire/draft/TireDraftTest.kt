package com.acme.transport.fleetops.tire.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireDraftTest {
    @Test
    fun `starts in requested`() {
        val item = newTireDraft("code-138", "label-776")
        assertEquals(TireDraftStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newTireDraft("code-138", "label-776")
        try {
            item.moveTo(TireDraftStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireDraftStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTireDraft("code-138", "label-776")
        item.status = TireDraftStatus.EXPIRED
        assertFails("move") { item.moveTo(TireDraftStatus.REQUESTED) }
    }
}
