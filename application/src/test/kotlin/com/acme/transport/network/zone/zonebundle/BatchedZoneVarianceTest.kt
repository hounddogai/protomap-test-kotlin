package com.acme.transport.network.zone.zonebundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedZoneVarianceTest {
    @Test
    fun `starts in planned`() {
        val item = newBatchedZoneVariance("code-500", "name-844")
        assertEquals(BatchedZoneVarianceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newBatchedZoneVariance("code-500", "name-844")
        try {
            item.moveTo(BatchedZoneVarianceStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedZoneVarianceStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBatchedZoneVariance("code-500", "name-844")
        item.status = BatchedZoneVarianceStatus.CANCELLED
        assertFails("move") { item.moveTo(BatchedZoneVarianceStatus.PLANNED) }
    }
}
