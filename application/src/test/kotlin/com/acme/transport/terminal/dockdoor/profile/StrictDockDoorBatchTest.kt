package com.acme.transport.terminal.dockdoor.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDockDoorBatchTest {
    @Test
    fun `starts in created`() {
        val item = newStrictDockDoorBatch("code-385", "title-194")
        assertEquals(StrictDockDoorBatchStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newStrictDockDoorBatch("code-385", "title-194")
        try {
            item.moveTo(StrictDockDoorBatchStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDockDoorBatchStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newStrictDockDoorBatch("code-385", "title-194")
        item.status = StrictDockDoorBatchStatus.RETIRED
        assertFails("move") { item.moveTo(StrictDockDoorBatchStatus.CREATED) }
    }
}
