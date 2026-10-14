package com.acme.transport.terminal.dwell.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellMetricTest {
    @Test
    fun `starts in proposed`() {
        val item = newDwellMetric("code-322", "label-7")
        assertEquals(DwellMetricStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newDwellMetric("code-322", "label-7")
        try {
            item.moveTo(DwellMetricStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellMetricStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDwellMetric("code-322", "label-7")
        item.status = DwellMetricStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DwellMetricStatus.PROPOSED) }
    }
}
