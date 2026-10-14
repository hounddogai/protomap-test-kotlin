package com.acme.transport.lastmile.redelivery.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryMetricTest {
    @Test
    fun `starts in new`() {
        val item = newRedeliveryMetric("code-977", "title-313")
        assertEquals(RedeliveryMetricStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newRedeliveryMetric("code-977", "title-313")
        try {
            item.moveTo(RedeliveryMetricStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryMetricStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRedeliveryMetric("code-977", "title-313")
        item.status = RedeliveryMetricStatus.FAILED
        assertFails("move") { item.moveTo(RedeliveryMetricStatus.NEW) }
    }
}
