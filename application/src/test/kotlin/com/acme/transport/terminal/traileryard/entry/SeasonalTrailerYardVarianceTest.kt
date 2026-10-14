package com.acme.transport.terminal.traileryard.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalTrailerYardVarianceTest {
    @Test
    fun `starts in draft`() {
        val item = newSeasonalTrailerYardVariance("code-878", "name-676")
        assertEquals(SeasonalTrailerYardVarianceStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newSeasonalTrailerYardVariance("code-878", "name-676")
        try {
            item.moveTo(SeasonalTrailerYardVarianceStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalTrailerYardVarianceStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSeasonalTrailerYardVariance("code-878", "name-676")
        item.status = SeasonalTrailerYardVarianceStatus.CLOSED
        assertFails("move") { item.moveTo(SeasonalTrailerYardVarianceStatus.DRAFT) }
    }
}
