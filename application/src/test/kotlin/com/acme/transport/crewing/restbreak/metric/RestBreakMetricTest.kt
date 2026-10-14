package com.acme.transport.crewing.restbreak.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakMetricTest {
    @Test
    fun `starts in proposed`() {
        val item = newRestBreakMetric("code-136", "name-910")
        assertEquals(RestBreakMetricStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newRestBreakMetric("code-136", "name-910")
        try {
            item.moveTo(RestBreakMetricStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakMetricStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRestBreakMetric("code-136", "name-910")
        item.status = RestBreakMetricStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RestBreakMetricStatus.PROPOSED) }
    }
}
