package com.acme.transport.emissions.routeoffset.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedRouteOffsetMappingTest {
    @Test
    fun `starts in planned`() {
        val item = newBatchedRouteOffsetMapping("code-848", "name-426")
        assertEquals(BatchedRouteOffsetMappingStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newBatchedRouteOffsetMapping("code-848", "name-426")
        try {
            item.moveTo(BatchedRouteOffsetMappingStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedRouteOffsetMappingStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBatchedRouteOffsetMapping("code-848", "name-426")
        item.status = BatchedRouteOffsetMappingStatus.CANCELLED
        assertFails("move") { item.moveTo(BatchedRouteOffsetMappingStatus.PLANNED) }
    }
}
