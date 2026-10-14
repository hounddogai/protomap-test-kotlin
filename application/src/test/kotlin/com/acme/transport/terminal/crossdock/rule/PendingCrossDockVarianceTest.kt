package com.acme.transport.terminal.crossdock.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingCrossDockVarianceTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingCrossDockVariance("code-365", "name-782")
        assertEquals(PendingCrossDockVarianceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newPendingCrossDockVariance("code-365", "name-782")
        try {
            item.moveTo(PendingCrossDockVarianceStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingCrossDockVarianceStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingCrossDockVariance("code-365", "name-782")
        item.status = PendingCrossDockVarianceStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingCrossDockVarianceStatus.PLANNED) }
    }
}
