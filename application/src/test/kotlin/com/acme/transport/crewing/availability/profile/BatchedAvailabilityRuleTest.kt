package com.acme.transport.crewing.availability.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedAvailabilityRuleTest {
    @Test
    fun `starts in draft`() {
        val item = newBatchedAvailabilityRule("code-387", "name-902")
        assertEquals(BatchedAvailabilityRuleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newBatchedAvailabilityRule("code-387", "name-902")
        try {
            item.moveTo(BatchedAvailabilityRuleStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedAvailabilityRuleStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBatchedAvailabilityRule("code-387", "name-902")
        item.status = BatchedAvailabilityRuleStatus.CLOSED
        assertFails("move") { item.moveTo(BatchedAvailabilityRuleStatus.DRAFT) }
    }
}
