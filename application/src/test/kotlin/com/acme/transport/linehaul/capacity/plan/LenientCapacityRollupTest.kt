package com.acme.transport.linehaul.capacity.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientCapacityRollupTest {
    @Test
    fun `starts in proposed`() {
        val item = newLenientCapacityRollup("code-55", "title-323")
        assertEquals(LenientCapacityRollupStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newLenientCapacityRollup("code-55", "title-323")
        try {
            item.moveTo(LenientCapacityRollupStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientCapacityRollupStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLenientCapacityRollup("code-55", "title-323")
        item.status = LenientCapacityRollupStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LenientCapacityRollupStatus.PROPOSED) }
    }
}
