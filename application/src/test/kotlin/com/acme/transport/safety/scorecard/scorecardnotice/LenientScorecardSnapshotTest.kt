package com.acme.transport.safety.scorecard.scorecardnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientScorecardSnapshotTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientScorecardSnapshot("code-458", "title-59")
        assertEquals(LenientScorecardSnapshotStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newLenientScorecardSnapshot("code-458", "title-59")
        try {
            item.moveTo(LenientScorecardSnapshotStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientScorecardSnapshotStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientScorecardSnapshot("code-458", "title-59")
        item.status = LenientScorecardSnapshotStatus.CLOSED
        assertFails("move") { item.moveTo(LenientScorecardSnapshotStatus.DRAFT) }
    }
}
