package com.acme.transport.network.zone.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackZoneSlotTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackZoneSlot("code-918", "title-281")
        assertEquals(FallbackZoneSlotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newFallbackZoneSlot("code-918", "title-281")
        try {
            item.moveTo(FallbackZoneSlotStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackZoneSlotStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackZoneSlot("code-918", "title-281")
        item.status = FallbackZoneSlotStatus.FAILED
        assertFails("move") { item.moveTo(FallbackZoneSlotStatus.NEW) }
    }
}
