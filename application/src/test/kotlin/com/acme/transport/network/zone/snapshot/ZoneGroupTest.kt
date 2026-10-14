package com.acme.transport.network.zone.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneGroupTest {
    @Test
    fun `starts in draft`() {
        val item = newZoneGroup("code-128", "title-628")
        assertEquals(ZoneGroupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newZoneGroup("code-128", "title-628")
        try {
            item.moveTo(ZoneGroupStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneGroupStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newZoneGroup("code-128", "title-628")
        item.status = ZoneGroupStatus.CLOSED
        assertFails("move") { item.moveTo(ZoneGroupStatus.DRAFT) }
    }
}
