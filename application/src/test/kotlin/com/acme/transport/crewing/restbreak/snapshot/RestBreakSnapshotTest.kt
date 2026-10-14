package com.acme.transport.crewing.restbreak.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakSnapshotTest {
    @Test
    fun `starts in new`() {
        val item = newRestBreakSnapshot("code-588", "title-189")
        assertEquals(RestBreakSnapshotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRestBreakSnapshot("code-588", "title-189")
        try {
            item.moveTo(RestBreakSnapshotStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakSnapshotStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRestBreakSnapshot("code-588", "title-189")
        item.status = RestBreakSnapshotStatus.FAILED
        assertFails("move") { item.moveTo(RestBreakSnapshotStatus.NEW) }
    }
}
