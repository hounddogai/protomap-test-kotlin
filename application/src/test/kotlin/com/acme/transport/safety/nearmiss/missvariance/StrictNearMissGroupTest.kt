package com.acme.transport.safety.nearmiss.missvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictNearMissGroupTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictNearMissGroup("code-386", "name-417")
        assertEquals(StrictNearMissGroupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newStrictNearMissGroup("code-386", "name-417")
        try {
            item.moveTo(StrictNearMissGroupStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictNearMissGroupStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictNearMissGroup("code-386", "name-417")
        item.status = StrictNearMissGroupStatus.CLOSED
        assertFails("move") { item.moveTo(StrictNearMissGroupStatus.DRAFT) }
    }
}
