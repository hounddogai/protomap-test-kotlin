package com.acme.transport.safety.coaching.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyCoachingSnapshotTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyCoachingSnapshot("code-623", "title-427")
        assertEquals(LegacyCoachingSnapshotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newLegacyCoachingSnapshot("code-623", "title-427")
        try {
            item.moveTo(LegacyCoachingSnapshotStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyCoachingSnapshotStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyCoachingSnapshot("code-623", "title-427")
        item.status = LegacyCoachingSnapshotStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyCoachingSnapshotStatus.IDLE) }
    }
}
