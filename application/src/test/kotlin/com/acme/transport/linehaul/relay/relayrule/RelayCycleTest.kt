package com.acme.transport.linehaul.relay.relayrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayCycleTest {
    @Test
    fun `starts in planned`() {
        val item = newRelayCycle("code-597", "label-623")
        assertEquals(RelayCycleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRelayCycle("code-597", "label-623")
        try {
            item.moveTo(RelayCycleStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayCycleStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRelayCycle("code-597", "label-623")
        item.status = RelayCycleStatus.CANCELLED
        assertFails("move") { item.moveTo(RelayCycleStatus.PLANNED) }
    }
}
