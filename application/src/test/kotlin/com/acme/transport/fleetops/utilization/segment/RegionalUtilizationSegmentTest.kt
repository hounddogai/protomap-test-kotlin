package com.acme.transport.fleetops.utilization.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalUtilizationSegmentTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalUtilizationSegment("code-102", "label-679")
        assertEquals(RegionalUtilizationSegmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRegionalUtilizationSegment("code-102", "label-679")
        try {
            item.moveTo(RegionalUtilizationSegmentStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalUtilizationSegmentStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalUtilizationSegment("code-102", "label-679")
        item.status = RegionalUtilizationSegmentStatus.FAILED
        assertFails("move") { item.moveTo(RegionalUtilizationSegmentStatus.NEW) }
    }
}
