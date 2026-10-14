package com.acme.transport.lastmile.redelivery.redeliveryentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalRedeliveryEstimateTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalRedeliveryEstimate("code-686", "label-553")
        assertEquals(SeasonalRedeliveryEstimateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newSeasonalRedeliveryEstimate("code-686", "label-553")
        try {
            item.moveTo(SeasonalRedeliveryEstimateStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalRedeliveryEstimateStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalRedeliveryEstimate("code-686", "label-553")
        item.status = SeasonalRedeliveryEstimateStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalRedeliveryEstimateStatus.REQUESTED) }
    }
}
