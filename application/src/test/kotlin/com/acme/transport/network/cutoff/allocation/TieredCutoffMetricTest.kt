package com.acme.transport.network.cutoff.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredCutoffMetricTest {
    @Test
    fun `starts in requested`() {
        val item = newTieredCutoffMetric("code-625", "label-934")
        assertEquals(TieredCutoffMetricStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newTieredCutoffMetric("code-625", "label-934")
        try {
            item.moveTo(TieredCutoffMetricStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredCutoffMetricStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTieredCutoffMetric("code-625", "label-934")
        item.status = TieredCutoffMetricStatus.EXPIRED
        assertFails("move") { item.moveTo(TieredCutoffMetricStatus.REQUESTED) }
    }
}
