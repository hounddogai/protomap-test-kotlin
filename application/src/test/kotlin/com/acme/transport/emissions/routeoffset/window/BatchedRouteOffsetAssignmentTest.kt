package com.acme.transport.emissions.routeoffset.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedRouteOffsetAssignmentTest {
    @Test
    fun `starts in created`() {
        val item = newBatchedRouteOffsetAssignment("code-143", "title-853")
        assertEquals(BatchedRouteOffsetAssignmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newBatchedRouteOffsetAssignment("code-143", "title-853")
        try {
            item.moveTo(BatchedRouteOffsetAssignmentStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedRouteOffsetAssignmentStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBatchedRouteOffsetAssignment("code-143", "title-853")
        item.status = BatchedRouteOffsetAssignmentStatus.RETIRED
        assertFails("move") { item.moveTo(BatchedRouteOffsetAssignmentStatus.CREATED) }
    }
}
