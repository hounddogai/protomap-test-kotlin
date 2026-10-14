package com.acme.transport.linehaul.tender.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderMetricTest {
    @Test
    fun `starts in open`() {
        val item = newTenderMetric("code-974", "label-651")
        assertEquals(TenderMetricStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newTenderMetric("code-974", "label-651")
        try {
            item.moveTo(TenderMetricStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderMetricStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTenderMetric("code-974", "label-651")
        item.status = TenderMetricStatus.ARCHIVED
        assertFails("move") { item.moveTo(TenderMetricStatus.OPEN) }
    }
}
