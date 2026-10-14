package com.acme.transport.lastmile.parcel.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualParcelMetricTest {
    @Test
    fun `starts in requested`() {
        val item = newManualParcelMetric("code-985", "name-163")
        assertEquals(ManualParcelMetricStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newManualParcelMetric("code-985", "name-163")
        try {
            item.moveTo(ManualParcelMetricStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualParcelMetricStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newManualParcelMetric("code-985", "name-163")
        item.status = ManualParcelMetricStatus.EXPIRED
        assertFails("move") { item.moveTo(ManualParcelMetricStatus.REQUESTED) }
    }
}
