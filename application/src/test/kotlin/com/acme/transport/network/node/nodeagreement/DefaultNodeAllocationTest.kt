package com.acme.transport.network.node.nodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultNodeAllocationTest {
    @Test
    fun `starts in draft`() {
        val item = newDefaultNodeAllocation("code-749", "title-559")
        assertEquals(DefaultNodeAllocationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newDefaultNodeAllocation("code-749", "title-559")
        try {
            item.moveTo(DefaultNodeAllocationStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultNodeAllocationStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDefaultNodeAllocation("code-749", "title-559")
        item.status = DefaultNodeAllocationStatus.CLOSED
        assertFails("move") { item.moveTo(DefaultNodeAllocationStatus.DRAFT) }
    }
}
