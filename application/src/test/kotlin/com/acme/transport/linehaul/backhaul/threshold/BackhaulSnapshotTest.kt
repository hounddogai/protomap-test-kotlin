package com.acme.transport.linehaul.backhaul.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulSnapshotTest {
    @Test
    fun `starts in created`() {
        val item = newBackhaulSnapshot("code-807", "name-818")
        assertEquals(BackhaulSnapshotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newBackhaulSnapshot("code-807", "name-818")
        try {
            item.moveTo(BackhaulSnapshotStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulSnapshotStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBackhaulSnapshot("code-807", "name-818")
        item.status = BackhaulSnapshotStatus.RETIRED
        assertFails("move") { item.moveTo(BackhaulSnapshotStatus.CREATED) }
    }
}
