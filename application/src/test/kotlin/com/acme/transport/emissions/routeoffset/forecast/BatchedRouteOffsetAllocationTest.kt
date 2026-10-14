package com.acme.transport.emissions.routeoffset.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedRouteOffsetAllocationTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedRouteOffsetAllocation("code-579", "title-92")
        assertEquals(BatchedRouteOffsetAllocationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newBatchedRouteOffsetAllocation("code-579", "title-92")
        try {
            item.moveTo(BatchedRouteOffsetAllocationStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedRouteOffsetAllocationStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedRouteOffsetAllocation("code-579", "title-92")
        item.status = BatchedRouteOffsetAllocationStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedRouteOffsetAllocationStatus.IDLE) }
    }
}
