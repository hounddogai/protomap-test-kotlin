package com.acme.transport.freightpay.ratecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledRateCardRollupTest {
    @Test
    fun `starts in planned`() {
        val item = newScheduledRateCardRollup("code-488", "name-751")
        assertEquals(ScheduledRateCardRollupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newScheduledRateCardRollup("code-488", "name-751")
        try {
            item.moveTo(ScheduledRateCardRollupStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledRateCardRollupStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScheduledRateCardRollup("code-488", "name-751")
        item.status = ScheduledRateCardRollupStatus.CANCELLED
        assertFails("move") { item.moveTo(ScheduledRateCardRollupStatus.PLANNED) }
    }
}
