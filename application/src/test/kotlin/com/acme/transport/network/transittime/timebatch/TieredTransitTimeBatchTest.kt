package com.acme.transport.network.transittime.timebatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredTransitTimeBatchTest {
    @Test
    fun `starts in planned`() {
        val item = newTieredTransitTimeBatch("code-371", "name-976")
        assertEquals(TieredTransitTimeBatchStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newTieredTransitTimeBatch("code-371", "name-976")
        try {
            item.moveTo(TieredTransitTimeBatchStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredTransitTimeBatchStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTieredTransitTimeBatch("code-371", "name-976")
        item.status = TieredTransitTimeBatchStatus.CANCELLED
        assertFails("move") { item.moveTo(TieredTransitTimeBatchStatus.PLANNED) }
    }
}
