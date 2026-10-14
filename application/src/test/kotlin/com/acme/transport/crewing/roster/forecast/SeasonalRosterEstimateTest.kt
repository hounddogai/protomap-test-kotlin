package com.acme.transport.crewing.roster.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalRosterEstimateTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalRosterEstimate("code-53", "title-617")
        assertEquals(SeasonalRosterEstimateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newSeasonalRosterEstimate("code-53", "title-617")
        try {
            item.moveTo(SeasonalRosterEstimateStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalRosterEstimateStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalRosterEstimate("code-53", "title-617")
        item.status = SeasonalRosterEstimateStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalRosterEstimateStatus.REQUESTED) }
    }
}
