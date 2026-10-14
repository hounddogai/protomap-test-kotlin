package com.acme.transport.safety.nearmiss.missadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedNearMissMappingTest {
    @Test
    fun `starts in created`() {
        val item = newBatchedNearMissMapping("code-384", "name-350")
        assertEquals(BatchedNearMissMappingStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newBatchedNearMissMapping("code-384", "name-350")
        try {
            item.moveTo(BatchedNearMissMappingStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedNearMissMappingStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBatchedNearMissMapping("code-384", "name-350")
        item.status = BatchedNearMissMappingStatus.RETIRED
        assertFails("move") { item.moveTo(BatchedNearMissMappingStatus.CREATED) }
    }
}
