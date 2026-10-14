package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryBackhaulMetricTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryBackhaulMetric("code-944", "label-254")
        assertEquals(PrimaryBackhaulMetricStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newPrimaryBackhaulMetric("code-944", "label-254")
        try {
            item.moveTo(PrimaryBackhaulMetricStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryBackhaulMetricStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryBackhaulMetric("code-944", "label-254")
        item.status = PrimaryBackhaulMetricStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryBackhaulMetricStatus.NEW) }
    }
}
