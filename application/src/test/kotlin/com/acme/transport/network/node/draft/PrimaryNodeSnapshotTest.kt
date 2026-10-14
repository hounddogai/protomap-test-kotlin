package com.acme.transport.network.node.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryNodeSnapshotTest {
    @Test
    fun `starts in created`() {
        val item = newPrimaryNodeSnapshot("code-847", "title-608")
        assertEquals(PrimaryNodeSnapshotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newPrimaryNodeSnapshot("code-847", "title-608")
        try {
            item.moveTo(PrimaryNodeSnapshotStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryNodeSnapshotStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPrimaryNodeSnapshot("code-847", "title-608")
        item.status = PrimaryNodeSnapshotStatus.RETIRED
        assertFails("move") { item.moveTo(PrimaryNodeSnapshotStatus.CREATED) }
    }
}
