package com.acme.transport.lastmile.redelivery.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalRedeliveryLedgerTest {
    @Test
    fun `starts in new`() {
        val item = newSeasonalRedeliveryLedger("code-580", "label-920")
        assertEquals(SeasonalRedeliveryLedgerStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newSeasonalRedeliveryLedger("code-580", "label-920")
        try {
            item.moveTo(SeasonalRedeliveryLedgerStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalRedeliveryLedgerStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSeasonalRedeliveryLedger("code-580", "label-920")
        item.status = SeasonalRedeliveryLedgerStatus.FAILED
        assertFails("move") { item.moveTo(SeasonalRedeliveryLedgerStatus.NEW) }
    }
}
