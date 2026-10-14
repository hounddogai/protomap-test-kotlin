package com.acme.transport.crewing.swap.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapSnapshotTest {
    @Test
    fun `starts in planned`() {
        val item = newSwapSnapshot("code-795", "title-470")
        assertEquals(SwapSnapshotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newSwapSnapshot("code-795", "title-470")
        try {
            item.moveTo(SwapSnapshotStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapSnapshotStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSwapSnapshot("code-795", "title-470")
        item.status = SwapSnapshotStatus.CANCELLED
        assertFails("move") { item.moveTo(SwapSnapshotStatus.PLANNED) }
    }
}
