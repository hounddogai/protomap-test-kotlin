package com.acme.transport.network.node.nodepolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryNodePolicyTest {
    @Test
    fun `starts in draft`() {
        val item = newPrimaryNodePolicy("code-608", "title-256")
        assertEquals(PrimaryNodePolicyStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newPrimaryNodePolicy("code-608", "title-256")
        try {
            item.moveTo(PrimaryNodePolicyStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryNodePolicyStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPrimaryNodePolicy("code-608", "title-256")
        item.status = PrimaryNodePolicyStatus.CLOSED
        assertFails("move") { item.moveTo(PrimaryNodePolicyStatus.DRAFT) }
    }
}
