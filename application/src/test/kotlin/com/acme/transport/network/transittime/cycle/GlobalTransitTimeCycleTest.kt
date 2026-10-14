package com.acme.transport.network.transittime.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalTransitTimeCycleTest {
    @Test
    fun `starts in created`() {
        val item = newGlobalTransitTimeCycle("code-937", "label-882")
        assertEquals(GlobalTransitTimeCycleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newGlobalTransitTimeCycle("code-937", "label-882")
        try {
            item.moveTo(GlobalTransitTimeCycleStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalTransitTimeCycleStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newGlobalTransitTimeCycle("code-937", "label-882")
        item.status = GlobalTransitTimeCycleStatus.RETIRED
        assertFails("move") { item.moveTo(GlobalTransitTimeCycleStatus.CREATED) }
    }
}
