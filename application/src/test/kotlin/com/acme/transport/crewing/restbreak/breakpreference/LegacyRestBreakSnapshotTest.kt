package com.acme.transport.crewing.restbreak.breakpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyRestBreakSnapshotTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyRestBreakSnapshot("code-680", "title-370")
        assertEquals(LegacyRestBreakSnapshotStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newLegacyRestBreakSnapshot("code-680", "title-370")
        try {
            item.moveTo(LegacyRestBreakSnapshotStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyRestBreakSnapshotStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyRestBreakSnapshot("code-680", "title-370")
        item.status = LegacyRestBreakSnapshotStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyRestBreakSnapshotStatus.PROPOSED) }
    }
}
