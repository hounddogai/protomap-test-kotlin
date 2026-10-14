package com.acme.transport.freightpay.ratecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalRateCardCheckpointTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalRateCardCheckpoint("code-363", "title-238")
        assertEquals(RegionalRateCardCheckpointStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRegionalRateCardCheckpoint("code-363", "title-238")
        try {
            item.moveTo(RegionalRateCardCheckpointStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalRateCardCheckpointStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalRateCardCheckpoint("code-363", "title-238")
        item.status = RegionalRateCardCheckpointStatus.FAILED
        assertFails("move") { item.moveTo(RegionalRateCardCheckpointStatus.NEW) }
    }
}
