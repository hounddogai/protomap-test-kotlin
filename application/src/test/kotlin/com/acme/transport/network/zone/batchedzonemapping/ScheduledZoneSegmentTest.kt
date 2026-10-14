package com.acme.transport.network.zone.batchedzonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledZoneSegmentTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledZoneSegment("code-674", "title-346")
        assertEquals(ScheduledZoneSegmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newScheduledZoneSegment("code-674", "title-346")
        try {
            item.moveTo(ScheduledZoneSegmentStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledZoneSegmentStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledZoneSegment("code-674", "title-346")
        item.status = ScheduledZoneSegmentStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledZoneSegmentStatus.CREATED) }
    }
}
