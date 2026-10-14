package com.acme.transport.customs.clearance.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredClearanceDraftTest {
    @Test
    fun `starts in draft`() {
        val item = newTieredClearanceDraft("code-741", "title-193")
        assertEquals(TieredClearanceDraftStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newTieredClearanceDraft("code-741", "title-193")
        try {
            item.moveTo(TieredClearanceDraftStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredClearanceDraftStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTieredClearanceDraft("code-741", "title-193")
        item.status = TieredClearanceDraftStatus.CLOSED
        assertFails("move") { item.moveTo(TieredClearanceDraftStatus.DRAFT) }
    }
}
