package com.acme.transport.crewing.restbreak.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalRestBreakLedgerTest {
    @Test
    fun `starts in new`() {
        val item = newSeasonalRestBreakLedger("code-283", "title-227")
        assertEquals(SeasonalRestBreakLedgerStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newSeasonalRestBreakLedger("code-283", "title-227")
        try {
            item.moveTo(SeasonalRestBreakLedgerStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalRestBreakLedgerStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSeasonalRestBreakLedger("code-283", "title-227")
        item.status = SeasonalRestBreakLedgerStatus.FAILED
        assertFails("move") { item.moveTo(SeasonalRestBreakLedgerStatus.NEW) }
    }
}
