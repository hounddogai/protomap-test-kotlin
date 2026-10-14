package com.acme.transport.emissions.idling.idlingtarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalIdlingProfileTest {
    @Test
    fun `starts in planned`() {
        val item = newSeasonalIdlingProfile("code-544", "label-242")
        assertEquals(SeasonalIdlingProfileStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newSeasonalIdlingProfile("code-544", "label-242")
        try {
            item.moveTo(SeasonalIdlingProfileStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalIdlingProfileStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSeasonalIdlingProfile("code-544", "label-242")
        item.status = SeasonalIdlingProfileStatus.CANCELLED
        assertFails("move") { item.moveTo(SeasonalIdlingProfileStatus.PLANNED) }
    }
}
