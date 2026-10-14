package com.acme.transport.emissions.routeoffset.offsetestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalRouteOffsetCycleTest {
    @Test
    fun `starts in draft`() {
        val item = newGlobalRouteOffsetCycle("code-409", "name-433")
        assertEquals(GlobalRouteOffsetCycleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newGlobalRouteOffsetCycle("code-409", "name-433")
        try {
            item.moveTo(GlobalRouteOffsetCycleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalRouteOffsetCycleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newGlobalRouteOffsetCycle("code-409", "name-433")
        item.status = GlobalRouteOffsetCycleStatus.CLOSED
        assertFails("move") { item.moveTo(GlobalRouteOffsetCycleStatus.DRAFT) }
    }
}
