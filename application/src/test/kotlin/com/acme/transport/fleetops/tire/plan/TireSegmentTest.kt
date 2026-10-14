package com.acme.transport.fleetops.tire.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireSegmentTest {
    @Test
    fun `starts in idle`() {
        val item = newTireSegment("code-517", "label-373")
        assertEquals(TireSegmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newTireSegment("code-517", "label-373")
        try {
            item.moveTo(TireSegmentStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireSegmentStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTireSegment("code-517", "label-373")
        item.status = TireSegmentStatus.STOPPED
        assertFails("move") { item.moveTo(TireSegmentStatus.IDLE) }
    }
}
