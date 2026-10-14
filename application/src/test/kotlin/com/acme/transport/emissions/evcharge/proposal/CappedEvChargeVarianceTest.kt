package com.acme.transport.emissions.evcharge.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedEvChargeVarianceTest {
    @Test
    fun `starts in new`() {
        val item = newCappedEvChargeVariance("code-495", "label-691")
        assertEquals(CappedEvChargeVarianceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCappedEvChargeVariance("code-495", "label-691")
        try {
            item.moveTo(CappedEvChargeVarianceStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedEvChargeVarianceStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCappedEvChargeVariance("code-495", "label-691")
        item.status = CappedEvChargeVarianceStatus.FAILED
        assertFails("move") { item.moveTo(CappedEvChargeVarianceStatus.NEW) }
    }
}
