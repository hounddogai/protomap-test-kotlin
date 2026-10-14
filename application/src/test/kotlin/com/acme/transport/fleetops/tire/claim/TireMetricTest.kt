package com.acme.transport.fleetops.tire.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireMetricTest {
    @Test
    fun `starts in new`() {
        val item = newTireMetric("code-878", "title-899")
        assertEquals(TireMetricStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newTireMetric("code-878", "title-899")
        try {
            item.moveTo(TireMetricStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireMetricStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTireMetric("code-878", "title-899")
        item.status = TireMetricStatus.FAILED
        assertFails("move") { item.moveTo(TireMetricStatus.NEW) }
    }
}
