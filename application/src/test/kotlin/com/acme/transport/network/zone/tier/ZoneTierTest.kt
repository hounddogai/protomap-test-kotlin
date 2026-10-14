package com.acme.transport.network.zone.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneTierTest {
    @Test
    fun `starts in idle`() {
        val item = newZoneTier("code-25", "label-784")
        assertEquals(ZoneTierStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newZoneTier("code-25", "label-784")
        try {
            item.moveTo(ZoneTierStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneTierStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newZoneTier("code-25", "label-784")
        item.status = ZoneTierStatus.STOPPED
        assertFails("move") { item.moveTo(ZoneTierStatus.IDLE) }
    }
}
