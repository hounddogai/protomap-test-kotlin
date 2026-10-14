package com.acme.transport.safety.coaching.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalCoachingSnapshotTest {
    @Test
    fun `starts in new`() {
        val item = newSeasonalCoachingSnapshot("code-614", "name-681")
        assertEquals(SeasonalCoachingSnapshotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newSeasonalCoachingSnapshot("code-614", "name-681")
        try {
            item.moveTo(SeasonalCoachingSnapshotStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalCoachingSnapshotStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSeasonalCoachingSnapshot("code-614", "name-681")
        item.status = SeasonalCoachingSnapshotStatus.FAILED
        assertFails("move") { item.moveTo(SeasonalCoachingSnapshotStatus.NEW) }
    }
}
