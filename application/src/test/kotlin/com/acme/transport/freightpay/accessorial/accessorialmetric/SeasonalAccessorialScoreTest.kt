package com.acme.transport.freightpay.accessorial.accessorialmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalAccessorialScoreTest {
    @Test
    fun `starts in planned`() {
        val item = newSeasonalAccessorialScore("code-68", "name-798")
        assertEquals(SeasonalAccessorialScoreStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newSeasonalAccessorialScore("code-68", "name-798")
        try {
            item.moveTo(SeasonalAccessorialScoreStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalAccessorialScoreStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSeasonalAccessorialScore("code-68", "name-798")
        item.status = SeasonalAccessorialScoreStatus.CANCELLED
        assertFails("move") { item.moveTo(SeasonalAccessorialScoreStatus.PLANNED) }
    }
}
