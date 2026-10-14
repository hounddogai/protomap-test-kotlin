package com.acme.transport.network.hub.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedHubRecordTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedHubRecord("code-107", "title-434")
        assertEquals(BatchedHubRecordStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newBatchedHubRecord("code-107", "title-434")
        try {
            item.moveTo(BatchedHubRecordStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedHubRecordStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedHubRecord("code-107", "title-434")
        item.status = BatchedHubRecordStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedHubRecordStatus.PROPOSED) }
    }
}
