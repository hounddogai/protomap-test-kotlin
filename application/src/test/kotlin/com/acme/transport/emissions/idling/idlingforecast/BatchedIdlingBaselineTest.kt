package com.acme.transport.emissions.idling.idlingforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedIdlingBaselineTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedIdlingBaseline("code-105", "title-111")
        assertEquals(BatchedIdlingBaselineStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newBatchedIdlingBaseline("code-105", "title-111")
        try {
            item.moveTo(BatchedIdlingBaselineStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedIdlingBaselineStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedIdlingBaseline("code-105", "title-111")
        item.status = BatchedIdlingBaselineStatus.FAILED
        assertFails("move") { item.moveTo(BatchedIdlingBaselineStatus.NEW) }
    }
}
