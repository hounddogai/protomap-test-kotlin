package com.acme.transport.safety.nearmiss.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictNearMissCycleTest {
    @Test
    fun `starts in created`() {
        val item = newStrictNearMissCycle("code-579", "title-470")
        assertEquals(StrictNearMissCycleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newStrictNearMissCycle("code-579", "title-470")
        try {
            item.moveTo(StrictNearMissCycleStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictNearMissCycleStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newStrictNearMissCycle("code-579", "title-470")
        item.status = StrictNearMissCycleStatus.RETIRED
        assertFails("move") { item.moveTo(StrictNearMissCycleStatus.CREATED) }
    }
}
