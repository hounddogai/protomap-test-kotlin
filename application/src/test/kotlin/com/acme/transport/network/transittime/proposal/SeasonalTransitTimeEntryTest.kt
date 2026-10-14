package com.acme.transport.network.transittime.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalTransitTimeEntryTest {
    @Test
    fun `starts in draft`() {
        val item = newSeasonalTransitTimeEntry("code-628", "label-682")
        assertEquals(SeasonalTransitTimeEntryStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newSeasonalTransitTimeEntry("code-628", "label-682")
        try {
            item.moveTo(SeasonalTransitTimeEntryStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalTransitTimeEntryStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSeasonalTransitTimeEntry("code-628", "label-682")
        item.status = SeasonalTransitTimeEntryStatus.CLOSED
        assertFails("move") { item.moveTo(SeasonalTransitTimeEntryStatus.DRAFT) }
    }
}
