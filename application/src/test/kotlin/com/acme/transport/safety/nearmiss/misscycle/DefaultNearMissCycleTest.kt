package com.acme.transport.safety.nearmiss.misscycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultNearMissCycleTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultNearMissCycle("code-63", "name-142")
        assertEquals(DefaultNearMissCycleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newDefaultNearMissCycle("code-63", "name-142")
        try {
            item.moveTo(DefaultNearMissCycleStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultNearMissCycleStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultNearMissCycle("code-63", "name-142")
        item.status = DefaultNearMissCycleStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultNearMissCycleStatus.IDLE) }
    }
}
