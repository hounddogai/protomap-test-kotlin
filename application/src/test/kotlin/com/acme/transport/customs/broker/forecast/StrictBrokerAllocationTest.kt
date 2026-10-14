package com.acme.transport.customs.broker.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictBrokerAllocationTest {
    @Test
    fun `starts in requested`() {
        val item = newStrictBrokerAllocation("code-828", "title-526")
        assertEquals(StrictBrokerAllocationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newStrictBrokerAllocation("code-828", "title-526")
        try {
            item.moveTo(StrictBrokerAllocationStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictBrokerAllocationStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newStrictBrokerAllocation("code-828", "title-526")
        item.status = StrictBrokerAllocationStatus.EXPIRED
        assertFails("move") { item.moveTo(StrictBrokerAllocationStatus.REQUESTED) }
    }
}
