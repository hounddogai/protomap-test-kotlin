package com.acme.transport.fleetops.tire.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyTireDraftTest {
    @Test
    fun `starts in open`() {
        val item = newLegacyTireDraft("code-219", "title-582")
        assertEquals(LegacyTireDraftStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newLegacyTireDraft("code-219", "title-582")
        try {
            item.moveTo(LegacyTireDraftStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyTireDraftStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLegacyTireDraft("code-219", "title-582")
        item.status = LegacyTireDraftStatus.ARCHIVED
        assertFails("move") { item.moveTo(LegacyTireDraftStatus.OPEN) }
    }
}
