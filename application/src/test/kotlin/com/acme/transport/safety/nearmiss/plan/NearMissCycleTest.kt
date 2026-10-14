package com.acme.transport.safety.nearmiss.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissCycleTest {
    @Test
    fun `starts in created`() {
        val item = newNearMissCycle("code-461", "label-415")
        assertEquals(NearMissCycleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newNearMissCycle("code-461", "label-415")
        try {
            item.moveTo(NearMissCycleStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissCycleStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newNearMissCycle("code-461", "label-415")
        item.status = NearMissCycleStatus.RETIRED
        assertFails("move") { item.moveTo(NearMissCycleStatus.CREATED) }
    }
}
