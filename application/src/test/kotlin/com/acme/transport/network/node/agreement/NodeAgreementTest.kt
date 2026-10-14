package com.acme.transport.network.node.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeAgreementTest {
    @Test
    fun `starts in planned`() {
        val item = newNodeAgreement("code-672", "name-303")
        assertEquals(NodeAgreementStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newNodeAgreement("code-672", "name-303")
        try {
            item.moveTo(NodeAgreementStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeAgreementStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newNodeAgreement("code-672", "name-303")
        item.status = NodeAgreementStatus.CANCELLED
        assertFails("move") { item.moveTo(NodeAgreementStatus.PLANNED) }
    }
}
