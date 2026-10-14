package com.acme.transport.freightpay.ratecard.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardSnapshotTest {
    @Test
    fun `starts in created`() {
        val item = newRateCardSnapshot("code-891", "label-157")
        assertEquals(RateCardSnapshotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRateCardSnapshot("code-891", "label-157")
        try {
            item.moveTo(RateCardSnapshotStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardSnapshotStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRateCardSnapshot("code-891", "label-157")
        item.status = RateCardSnapshotStatus.RETIRED
        assertFails("move") { item.moveTo(RateCardSnapshotStatus.CREATED) }
    }
}
