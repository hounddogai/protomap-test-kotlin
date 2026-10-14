package com.acme.transport.terminal.traileryard.yardforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalTrailerYardOverrideTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalTrailerYardOverride("code-339", "name-578")
        assertEquals(SeasonalTrailerYardOverrideStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newSeasonalTrailerYardOverride("code-339", "name-578")
        try {
            item.moveTo(SeasonalTrailerYardOverrideStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalTrailerYardOverrideStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalTrailerYardOverride("code-339", "name-578")
        item.status = SeasonalTrailerYardOverrideStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalTrailerYardOverrideStatus.REQUESTED) }
    }
}
