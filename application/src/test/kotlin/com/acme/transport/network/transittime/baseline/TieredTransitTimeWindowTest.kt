package com.acme.transport.network.transittime.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredTransitTimeWindowTest {
    @Test
    fun `starts in draft`() {
        val item = newTieredTransitTimeWindow("code-272", "label-22")
        assertEquals(TieredTransitTimeWindowStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newTieredTransitTimeWindow("code-272", "label-22")
        try {
            item.moveTo(TieredTransitTimeWindowStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredTransitTimeWindowStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTieredTransitTimeWindow("code-272", "label-22")
        item.status = TieredTransitTimeWindowStatus.CLOSED
        assertFails("move") { item.moveTo(TieredTransitTimeWindowStatus.DRAFT) }
    }
}
