package com.acme.transport.terminal.dwell.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDwellProfileTest {
    @Test
    fun `starts in planned`() {
        val item = newSeasonalDwellProfile("code-494", "label-224")
        assertEquals(SeasonalDwellProfileStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newSeasonalDwellProfile("code-494", "label-224")
        try {
            item.moveTo(SeasonalDwellProfileStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDwellProfileStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSeasonalDwellProfile("code-494", "label-224")
        item.status = SeasonalDwellProfileStatus.CANCELLED
        assertFails("move") { item.moveTo(SeasonalDwellProfileStatus.PLANNED) }
    }
}
