package com.acme.transport.crewing.swap.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalSwapRollupTest {
    @Test
    fun `starts in planned`() {
        val item = newSeasonalSwapRollup("code-421", "title-388")
        assertEquals(SeasonalSwapRollupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newSeasonalSwapRollup("code-421", "title-388")
        try {
            item.moveTo(SeasonalSwapRollupStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalSwapRollupStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSeasonalSwapRollup("code-421", "title-388")
        item.status = SeasonalSwapRollupStatus.CANCELLED
        assertFails("move") { item.moveTo(SeasonalSwapRollupStatus.PLANNED) }
    }
}
