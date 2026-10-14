package com.acme.transport.fleetops.tire.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedTireSegmentTest {
    @Test
    fun `starts in created`() {
        val item = newWeightedTireSegment("code-266", "name-615")
        assertEquals(WeightedTireSegmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newWeightedTireSegment("code-266", "name-615")
        try {
            item.moveTo(WeightedTireSegmentStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedTireSegmentStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newWeightedTireSegment("code-266", "name-615")
        item.status = WeightedTireSegmentStatus.RETIRED
        assertFails("move") { item.moveTo(WeightedTireSegmentStatus.CREATED) }
    }
}
