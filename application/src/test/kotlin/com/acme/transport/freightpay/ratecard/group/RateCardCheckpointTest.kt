package com.acme.transport.freightpay.ratecard.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardCheckpointTest {
    @Test
    fun `starts in proposed`() {
        val item = newRateCardCheckpoint("code-719", "label-913")
        assertEquals(RateCardCheckpointStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRateCardCheckpoint("code-719", "label-913")
        try {
            item.moveTo(RateCardCheckpointStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardCheckpointStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRateCardCheckpoint("code-719", "label-913")
        item.status = RateCardCheckpointStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RateCardCheckpointStatus.PROPOSED) }
    }
}
