package com.acme.transport.terminal.dockdoor.doorclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorBatchTest {
    @Test
    fun `starts in created`() {
        val item = newDockDoorBatch("code-86", "title-790")
        assertEquals(DockDoorBatchStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newDockDoorBatch("code-86", "title-790")
        try {
            item.moveTo(DockDoorBatchStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorBatchStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDockDoorBatch("code-86", "title-790")
        item.status = DockDoorBatchStatus.RETIRED
        assertFails("move") { item.moveTo(DockDoorBatchStatus.CREATED) }
    }
}
