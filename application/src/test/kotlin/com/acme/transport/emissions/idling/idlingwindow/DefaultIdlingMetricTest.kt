package com.acme.transport.emissions.idling.idlingwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultIdlingMetricTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultIdlingMetric("code-143", "title-308")
        assertEquals(DefaultIdlingMetricStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newDefaultIdlingMetric("code-143", "title-308")
        try {
            item.moveTo(DefaultIdlingMetricStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultIdlingMetricStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultIdlingMetric("code-143", "title-308")
        item.status = DefaultIdlingMetricStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultIdlingMetricStatus.CREATED) }
    }
}
