package com.acme.transport.network.transittime.timenotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredTransitTimeVarianceTest {
    @Test
    fun `starts in new`() {
        val item = newTieredTransitTimeVariance("code-85", "name-576")
        assertEquals(TieredTransitTimeVarianceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newTieredTransitTimeVariance("code-85", "name-576")
        try {
            item.moveTo(TieredTransitTimeVarianceStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredTransitTimeVarianceStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTieredTransitTimeVariance("code-85", "name-576")
        item.status = TieredTransitTimeVarianceStatus.FAILED
        assertFails("move") { item.moveTo(TieredTransitTimeVarianceStatus.NEW) }
    }
}
