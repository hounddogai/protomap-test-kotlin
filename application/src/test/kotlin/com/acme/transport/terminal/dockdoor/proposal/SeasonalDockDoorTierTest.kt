package com.acme.transport.terminal.dockdoor.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDockDoorTierTest {
    @Test
    fun `starts in planned`() {
        val item = newSeasonalDockDoorTier("code-35", "name-664")
        assertEquals(SeasonalDockDoorTierStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newSeasonalDockDoorTier("code-35", "name-664")
        try {
            item.moveTo(SeasonalDockDoorTierStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDockDoorTierStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSeasonalDockDoorTier("code-35", "name-664")
        item.status = SeasonalDockDoorTierStatus.CANCELLED
        assertFails("move") { item.moveTo(SeasonalDockDoorTierStatus.PLANNED) }
    }
}
