package com.acme.transport.network.zone.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneWindowTest {
    @Test
    fun `starts in draft`() {
        val item = newZoneWindow("code-638", "title-784")
        assertEquals(ZoneWindowStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newZoneWindow("code-638", "title-784")
        try {
            item.moveTo(ZoneWindowStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneWindowStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newZoneWindow("code-638", "title-784")
        item.status = ZoneWindowStatus.CLOSED
        assertFails("move") { item.moveTo(ZoneWindowStatus.DRAFT) }
    }
}
