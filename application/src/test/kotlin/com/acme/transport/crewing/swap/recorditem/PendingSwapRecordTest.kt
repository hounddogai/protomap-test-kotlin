package com.acme.transport.crewing.swap.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingSwapRecordTest {
    @Test
    fun `starts in new`() {
        val item = newPendingSwapRecord("code-762", "label-436")
        assertEquals(PendingSwapRecordStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newPendingSwapRecord("code-762", "label-436")
        try {
            item.moveTo(PendingSwapRecordStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingSwapRecordStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPendingSwapRecord("code-762", "label-436")
        item.status = PendingSwapRecordStatus.FAILED
        assertFails("move") { item.moveTo(PendingSwapRecordStatus.NEW) }
    }
}
