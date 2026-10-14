package com.acme.transport.network.zone.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneSlotTest {
    @Test
    fun `starts in open`() {
        val item = newZoneSlot("code-391", "title-981")
        assertEquals(ZoneSlotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newZoneSlot("code-391", "title-981")
        try {
            item.moveTo(ZoneSlotStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneSlotStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newZoneSlot("code-391", "title-981")
        item.status = ZoneSlotStatus.ARCHIVED
        assertFails("move") { item.moveTo(ZoneSlotStatus.OPEN) }
    }
}
