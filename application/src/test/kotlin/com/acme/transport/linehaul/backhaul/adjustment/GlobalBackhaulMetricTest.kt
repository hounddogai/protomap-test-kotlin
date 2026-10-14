package com.acme.transport.linehaul.backhaul.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalBackhaulMetricTest {
    @Test
    fun `starts in planned`() {
        val item = newGlobalBackhaulMetric("code-441", "label-589")
        assertEquals(GlobalBackhaulMetricStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newGlobalBackhaulMetric("code-441", "label-589")
        try {
            item.moveTo(GlobalBackhaulMetricStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalBackhaulMetricStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newGlobalBackhaulMetric("code-441", "label-589")
        item.status = GlobalBackhaulMetricStatus.CANCELLED
        assertFails("move") { item.moveTo(GlobalBackhaulMetricStatus.PLANNED) }
    }
}
