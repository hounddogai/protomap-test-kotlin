package com.acme.transport.terminal.dockdoor.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDockDoorScoreTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalDockDoorScore("code-720", "name-595")
        assertEquals(SeasonalDockDoorScoreStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newSeasonalDockDoorScore("code-720", "name-595")
        try {
            item.moveTo(SeasonalDockDoorScoreStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDockDoorScoreStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalDockDoorScore("code-720", "name-595")
        item.status = SeasonalDockDoorScoreStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalDockDoorScoreStatus.REQUESTED) }
    }
}
