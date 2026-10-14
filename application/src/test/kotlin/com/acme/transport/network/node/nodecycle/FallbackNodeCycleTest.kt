package com.acme.transport.network.node.nodecycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackNodeCycleTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackNodeCycle("code-994", "name-653")
        assertEquals(FallbackNodeCycleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newFallbackNodeCycle("code-994", "name-653")
        try {
            item.moveTo(FallbackNodeCycleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackNodeCycleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackNodeCycle("code-994", "name-653")
        item.status = FallbackNodeCycleStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackNodeCycleStatus.DRAFT) }
    }
}
