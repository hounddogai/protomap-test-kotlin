package com.acme.transport.network.node.nodescore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedNodeForecastTest {
    @Test
    fun `starts in created`() {
        val item = newBatchedNodeForecast("code-298", "title-852")
        assertEquals(BatchedNodeForecastStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newBatchedNodeForecast("code-298", "title-852")
        try {
            item.moveTo(BatchedNodeForecastStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedNodeForecastStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBatchedNodeForecast("code-298", "title-852")
        item.status = BatchedNodeForecastStatus.RETIRED
        assertFails("move") { item.moveTo(BatchedNodeForecastStatus.CREATED) }
    }
}
