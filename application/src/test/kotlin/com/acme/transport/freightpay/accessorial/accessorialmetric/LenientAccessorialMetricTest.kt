package com.acme.transport.freightpay.accessorial.accessorialmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientAccessorialMetricTest {
    @Test
    fun `starts in open`() {
        val item = newLenientAccessorialMetric("code-568", "label-392")
        assertEquals(LenientAccessorialMetricStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newLenientAccessorialMetric("code-568", "label-392")
        try {
            item.moveTo(LenientAccessorialMetricStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientAccessorialMetricStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientAccessorialMetric("code-568", "label-392")
        item.status = LenientAccessorialMetricStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientAccessorialMetricStatus.OPEN) }
    }
}
