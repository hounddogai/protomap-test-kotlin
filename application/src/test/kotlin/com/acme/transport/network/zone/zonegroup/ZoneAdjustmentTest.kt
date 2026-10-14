package com.acme.transport.network.zone.zonegroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneAdjustmentTest {
    @Test
    fun `starts in idle`() {
        val item = newZoneAdjustment("code-840", "title-78")
        assertEquals(ZoneAdjustmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newZoneAdjustment("code-840", "title-78")
        try {
            item.moveTo(ZoneAdjustmentStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneAdjustmentStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newZoneAdjustment("code-840", "title-78")
        item.status = ZoneAdjustmentStatus.STOPPED
        assertFails("move") { item.moveTo(ZoneAdjustmentStatus.IDLE) }
    }
}
