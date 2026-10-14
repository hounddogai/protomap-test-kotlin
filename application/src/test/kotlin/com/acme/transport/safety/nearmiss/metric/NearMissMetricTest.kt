package com.acme.transport.safety.nearmiss.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissMetricTest {
    @Test
    fun `starts in idle`() {
        val item = newNearMissMetric("code-587", "label-827")
        assertEquals(NearMissMetricStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newNearMissMetric("code-587", "label-827")
        try {
            item.moveTo(NearMissMetricStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissMetricStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newNearMissMetric("code-587", "label-827")
        item.status = NearMissMetricStatus.STOPPED
        assertFails("move") { item.moveTo(NearMissMetricStatus.IDLE) }
    }
}
