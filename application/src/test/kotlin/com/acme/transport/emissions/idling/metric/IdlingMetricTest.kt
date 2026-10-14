package com.acme.transport.emissions.idling.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingMetricTest {
    @Test
    fun `starts in new`() {
        val item = newIdlingMetric("code-783", "title-935")
        assertEquals(IdlingMetricStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newIdlingMetric("code-783", "title-935")
        try {
            item.moveTo(IdlingMetricStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingMetricStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newIdlingMetric("code-783", "title-935")
        item.status = IdlingMetricStatus.FAILED
        assertFails("move") { item.moveTo(IdlingMetricStatus.NEW) }
    }
}
