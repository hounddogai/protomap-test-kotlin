package com.acme.transport.linehaul.tender.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultTenderMetricTest {
    @Test
    fun `starts in planned`() {
        val item = newDefaultTenderMetric("code-382", "label-813")
        assertEquals(DefaultTenderMetricStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newDefaultTenderMetric("code-382", "label-813")
        try {
            item.moveTo(DefaultTenderMetricStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultTenderMetricStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDefaultTenderMetric("code-382", "label-813")
        item.status = DefaultTenderMetricStatus.CANCELLED
        assertFails("move") { item.moveTo(DefaultTenderMetricStatus.PLANNED) }
    }
}
