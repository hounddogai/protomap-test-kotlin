package com.acme.transport.freightpay.ratecard.cardsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalRateCardSnapshotTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalRateCardSnapshot("code-73", "name-206")
        assertEquals(SeasonalRateCardSnapshotStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newSeasonalRateCardSnapshot("code-73", "name-206")
        try {
            item.moveTo(SeasonalRateCardSnapshotStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalRateCardSnapshotStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalRateCardSnapshot("code-73", "name-206")
        item.status = SeasonalRateCardSnapshotStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalRateCardSnapshotStatus.REQUESTED) }
    }
}
