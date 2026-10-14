package com.acme.transport.network.hub.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyHubSlotTest {
    @Test
    fun `starts in draft`() {
        val item = newLegacyHubSlot("code-124", "label-956")
        assertEquals(LegacyHubSlotStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newLegacyHubSlot("code-124", "label-956")
        try {
            item.moveTo(LegacyHubSlotStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyHubSlotStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLegacyHubSlot("code-124", "label-956")
        item.status = LegacyHubSlotStatus.CLOSED
        assertFails("move") { item.moveTo(LegacyHubSlotStatus.DRAFT) }
    }
}
