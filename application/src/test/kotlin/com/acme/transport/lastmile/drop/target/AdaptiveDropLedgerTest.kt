package com.acme.transport.lastmile.drop.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveDropLedgerTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveDropLedger("code-930", "title-345")
        assertEquals(AdaptiveDropLedgerStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newAdaptiveDropLedger("code-930", "title-345")
        try {
            item.moveTo(AdaptiveDropLedgerStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveDropLedgerStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveDropLedger("code-930", "title-345")
        item.status = AdaptiveDropLedgerStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveDropLedgerStatus.IDLE) }
    }
}
