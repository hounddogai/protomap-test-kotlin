package com.acme.transport.crewing.swap.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingSwapSnapshotTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingSwapSnapshot("code-762", "title-501")
        assertEquals(RollingSwapSnapshotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newRollingSwapSnapshot("code-762", "title-501")
        try {
            item.moveTo(RollingSwapSnapshotStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingSwapSnapshotStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingSwapSnapshot("code-762", "title-501")
        item.status = RollingSwapSnapshotStatus.STOPPED
        assertFails("move") { item.moveTo(RollingSwapSnapshotStatus.IDLE) }
    }
}
