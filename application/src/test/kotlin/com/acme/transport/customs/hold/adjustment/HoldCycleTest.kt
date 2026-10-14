package com.acme.transport.customs.hold.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldCycleTest {
    @Test
    fun `starts in planned`() {
        val item = newHoldCycle("code-893", "label-566")
        assertEquals(HoldCycleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newHoldCycle("code-893", "label-566")
        try {
            item.moveTo(HoldCycleStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldCycleStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newHoldCycle("code-893", "label-566")
        item.status = HoldCycleStatus.CANCELLED
        assertFails("move") { item.moveTo(HoldCycleStatus.PLANNED) }
    }
}
