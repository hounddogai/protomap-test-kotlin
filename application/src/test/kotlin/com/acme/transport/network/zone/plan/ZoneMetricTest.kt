package com.acme.transport.network.zone.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneMetricTest {
    @Test
    fun `starts in draft`() {
        val item = newZoneMetric("code-644", "name-388")
        assertEquals(ZoneMetricStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newZoneMetric("code-644", "name-388")
        try {
            item.moveTo(ZoneMetricStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneMetricStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newZoneMetric("code-644", "name-388")
        item.status = ZoneMetricStatus.CLOSED
        assertFails("move") { item.moveTo(ZoneMetricStatus.DRAFT) }
    }
}
