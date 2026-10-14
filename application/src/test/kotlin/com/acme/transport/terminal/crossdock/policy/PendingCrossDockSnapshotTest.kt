package com.acme.transport.terminal.crossdock.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingCrossDockSnapshotTest {
    @Test
    fun `starts in created`() {
        val item = newPendingCrossDockSnapshot("code-356", "label-225")
        assertEquals(PendingCrossDockSnapshotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newPendingCrossDockSnapshot("code-356", "label-225")
        try {
            item.moveTo(PendingCrossDockSnapshotStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingCrossDockSnapshotStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPendingCrossDockSnapshot("code-356", "label-225")
        item.status = PendingCrossDockSnapshotStatus.RETIRED
        assertFails("move") { item.moveTo(PendingCrossDockSnapshotStatus.CREATED) }
    }
}
