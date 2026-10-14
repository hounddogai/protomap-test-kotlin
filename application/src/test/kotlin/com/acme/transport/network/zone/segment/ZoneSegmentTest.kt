package com.acme.transport.network.zone.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneSegmentTest {
    @Test
    fun `starts in planned`() {
        val item = newZoneSegment("code-226", "name-181")
        assertEquals(ZoneSegmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newZoneSegment("code-226", "name-181")
        try {
            item.moveTo(ZoneSegmentStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneSegmentStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newZoneSegment("code-226", "name-181")
        item.status = ZoneSegmentStatus.CANCELLED
        assertFails("move") { item.moveTo(ZoneSegmentStatus.PLANNED) }
    }
}
