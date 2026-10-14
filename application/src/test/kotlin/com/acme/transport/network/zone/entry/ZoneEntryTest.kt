package com.acme.transport.network.zone.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneEntryTest {
    @Test
    fun `starts in new`() {
        val item = newZoneEntry("code-534", "title-154")
        assertEquals(ZoneEntryStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newZoneEntry("code-534", "title-154")
        try {
            item.moveTo(ZoneEntryStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneEntryStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newZoneEntry("code-534", "title-154")
        item.status = ZoneEntryStatus.FAILED
        assertFails("move") { item.moveTo(ZoneEntryStatus.NEW) }
    }
}
