package com.acme.transport.network.zone.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneTargetTest {
    @Test
    fun `starts in idle`() {
        val item = newZoneTarget("code-218", "title-751")
        assertEquals(ZoneTargetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newZoneTarget("code-218", "title-751")
        try {
            item.moveTo(ZoneTargetStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneTargetStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newZoneTarget("code-218", "title-751")
        item.status = ZoneTargetStatus.STOPPED
        assertFails("move") { item.moveTo(ZoneTargetStatus.IDLE) }
    }
}
