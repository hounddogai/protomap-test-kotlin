package com.acme.transport.lastmile.parcel.parcelmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultParcelMetricTest {
    @Test
    fun `starts in open`() {
        val item = newDefaultParcelMetric("code-165", "title-327")
        assertEquals(DefaultParcelMetricStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newDefaultParcelMetric("code-165", "title-327")
        try {
            item.moveTo(DefaultParcelMetricStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultParcelMetricStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDefaultParcelMetric("code-165", "title-327")
        item.status = DefaultParcelMetricStatus.ARCHIVED
        assertFails("move") { item.moveTo(DefaultParcelMetricStatus.OPEN) }
    }
}
