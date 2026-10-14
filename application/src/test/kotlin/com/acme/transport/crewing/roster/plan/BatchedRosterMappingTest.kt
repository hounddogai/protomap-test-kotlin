package com.acme.transport.crewing.roster.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedRosterMappingTest {
    @Test
    fun `starts in created`() {
        val item = newBatchedRosterMapping("code-584", "name-735")
        assertEquals(BatchedRosterMappingStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newBatchedRosterMapping("code-584", "name-735")
        try {
            item.moveTo(BatchedRosterMappingStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedRosterMappingStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBatchedRosterMapping("code-584", "name-735")
        item.status = BatchedRosterMappingStatus.RETIRED
        assertFails("move") { item.moveTo(BatchedRosterMappingStatus.CREATED) }
    }
}
