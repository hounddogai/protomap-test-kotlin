package com.acme.transport.lastmile.parcel.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalParcelLedgerTest {
    @Test
    fun `starts in idle`() {
        val item = newSeasonalParcelLedger("code-840", "name-674")
        assertEquals(SeasonalParcelLedgerStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newSeasonalParcelLedger("code-840", "name-674")
        try {
            item.moveTo(SeasonalParcelLedgerStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalParcelLedgerStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSeasonalParcelLedger("code-840", "name-674")
        item.status = SeasonalParcelLedgerStatus.STOPPED
        assertFails("move") { item.moveTo(SeasonalParcelLedgerStatus.IDLE) }
    }
}
