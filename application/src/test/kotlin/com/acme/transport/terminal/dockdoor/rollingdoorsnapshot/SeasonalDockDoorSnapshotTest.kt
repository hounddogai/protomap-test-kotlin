package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDockDoorSnapshotTest {
    @Test
    fun `starts in created`() {
        val item = newSeasonalDockDoorSnapshot("code-473", "title-608")
        assertEquals(SeasonalDockDoorSnapshotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newSeasonalDockDoorSnapshot("code-473", "title-608")
        try {
            item.moveTo(SeasonalDockDoorSnapshotStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDockDoorSnapshotStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSeasonalDockDoorSnapshot("code-473", "title-608")
        item.status = SeasonalDockDoorSnapshotStatus.RETIRED
        assertFails("move") { item.moveTo(SeasonalDockDoorSnapshotStatus.CREATED) }
    }
}
