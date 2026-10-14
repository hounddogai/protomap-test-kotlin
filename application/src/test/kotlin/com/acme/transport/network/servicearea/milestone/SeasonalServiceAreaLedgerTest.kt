package com.acme.transport.network.servicearea.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalServiceAreaLedgerTest {
    @Test
    fun `starts in idle`() {
        val item = newSeasonalServiceAreaLedger("code-850", "title-878")
        assertEquals(SeasonalServiceAreaLedgerStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newSeasonalServiceAreaLedger("code-850", "title-878")
        try {
            item.moveTo(SeasonalServiceAreaLedgerStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalServiceAreaLedgerStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSeasonalServiceAreaLedger("code-850", "title-878")
        item.status = SeasonalServiceAreaLedgerStatus.STOPPED
        assertFails("move") { item.moveTo(SeasonalServiceAreaLedgerStatus.IDLE) }
    }
}
