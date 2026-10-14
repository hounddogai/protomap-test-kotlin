package com.acme.transport.network.zone.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalZoneSegmentTest {
    @Test
    fun `starts in open`() {
        val item = newRegionalZoneSegment("code-470", "title-57")
        assertEquals(RegionalZoneSegmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newRegionalZoneSegment("code-470", "title-57")
        try {
            item.moveTo(RegionalZoneSegmentStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalZoneSegmentStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRegionalZoneSegment("code-470", "title-57")
        item.status = RegionalZoneSegmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(RegionalZoneSegmentStatus.OPEN) }
    }
}
