package com.acme.transport.network.zone.zonequota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingZoneWindowTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingZoneWindow("code-991", "title-558")
        assertEquals(RollingZoneWindowStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRollingZoneWindow("code-991", "title-558")
        try {
            item.moveTo(RollingZoneWindowStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingZoneWindowStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingZoneWindow("code-991", "title-558")
        item.status = RollingZoneWindowStatus.CLOSED
        assertFails("move") { item.moveTo(RollingZoneWindowStatus.DRAFT) }
    }
}
