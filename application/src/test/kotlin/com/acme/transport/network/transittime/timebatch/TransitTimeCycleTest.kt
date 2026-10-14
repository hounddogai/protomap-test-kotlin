package com.acme.transport.network.transittime.timebatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeCycleTest {
    @Test
    fun `starts in planned`() {
        val item = newTransitTimeCycle("code-894", "name-548")
        assertEquals(TransitTimeCycleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newTransitTimeCycle("code-894", "name-548")
        try {
            item.moveTo(TransitTimeCycleStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeCycleStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTransitTimeCycle("code-894", "name-548")
        item.status = TransitTimeCycleStatus.CANCELLED
        assertFails("move") { item.moveTo(TransitTimeCycleStatus.PLANNED) }
    }
}
