package com.acme.transport.network.node.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedNodeWindowTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedNodeWindow("code-847", "name-508")
        assertEquals(WeightedNodeWindowStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newWeightedNodeWindow("code-847", "name-508")
        try {
            item.moveTo(WeightedNodeWindowStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedNodeWindowStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedNodeWindow("code-847", "name-508")
        item.status = WeightedNodeWindowStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedNodeWindowStatus.REQUESTED) }
    }
}
