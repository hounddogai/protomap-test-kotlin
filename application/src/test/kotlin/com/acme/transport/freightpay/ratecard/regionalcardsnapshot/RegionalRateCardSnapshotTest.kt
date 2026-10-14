package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalRateCardSnapshotTest {
    @Test
    fun `starts in draft`() {
        val item = newRegionalRateCardSnapshot("code-153", "name-15")
        assertEquals(RegionalRateCardSnapshotStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newRegionalRateCardSnapshot("code-153", "name-15")
        try {
            item.moveTo(RegionalRateCardSnapshotStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalRateCardSnapshotStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRegionalRateCardSnapshot("code-153", "name-15")
        item.status = RegionalRateCardSnapshotStatus.CLOSED
        assertFails("move") { item.moveTo(RegionalRateCardSnapshotStatus.DRAFT) }
    }
}
