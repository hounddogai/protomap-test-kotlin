package com.acme.transport.emissions.idling.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveIdlingLedgerTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveIdlingLedger("code-763", "label-402")
        assertEquals(AdaptiveIdlingLedgerStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newAdaptiveIdlingLedger("code-763", "label-402")
        try {
            item.moveTo(AdaptiveIdlingLedgerStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveIdlingLedgerStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveIdlingLedger("code-763", "label-402")
        item.status = AdaptiveIdlingLedgerStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveIdlingLedgerStatus.NEW) }
    }
}
