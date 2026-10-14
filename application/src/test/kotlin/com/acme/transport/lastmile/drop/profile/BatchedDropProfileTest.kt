package com.acme.transport.lastmile.drop.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedDropProfileTest {
    @Test
    fun `starts in created`() {
        val item = newBatchedDropProfile("code-385", "label-442")
        assertEquals(BatchedDropProfileStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newBatchedDropProfile("code-385", "label-442")
        try {
            item.moveTo(BatchedDropProfileStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedDropProfileStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBatchedDropProfile("code-385", "label-442")
        item.status = BatchedDropProfileStatus.RETIRED
        assertFails("move") { item.moveTo(BatchedDropProfileStatus.CREATED) }
    }
}
