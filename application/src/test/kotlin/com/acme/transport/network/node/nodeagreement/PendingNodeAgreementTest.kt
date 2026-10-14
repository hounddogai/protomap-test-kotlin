package com.acme.transport.network.node.nodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingNodeAgreementTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingNodeAgreement("code-608", "label-338")
        assertEquals(PendingNodeAgreementStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newPendingNodeAgreement("code-608", "label-338")
        try {
            item.moveTo(PendingNodeAgreementStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingNodeAgreementStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingNodeAgreement("code-608", "label-338")
        item.status = PendingNodeAgreementStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingNodeAgreementStatus.REQUESTED) }
    }
}
