package com.acme.transport.terminal.dockdoor.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDockDoorAdjustmentTest {
    @Test
    fun `starts in idle`() {
        val item = newSeasonalDockDoorAdjustment("code-859", "label-370")
        assertEquals(SeasonalDockDoorAdjustmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newSeasonalDockDoorAdjustment("code-859", "label-370")
        try {
            item.moveTo(SeasonalDockDoorAdjustmentStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDockDoorAdjustmentStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSeasonalDockDoorAdjustment("code-859", "label-370")
        item.status = SeasonalDockDoorAdjustmentStatus.STOPPED
        assertFails("move") { item.moveTo(SeasonalDockDoorAdjustmentStatus.IDLE) }
    }
}
