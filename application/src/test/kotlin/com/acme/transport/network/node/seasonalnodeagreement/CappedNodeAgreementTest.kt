package com.acme.transport.network.node.seasonalnodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedNodeAgreementTest {
    @Test
    fun `starts in idle`() {
        val item = newCappedNodeAgreement("code-326", "title-60")
        assertEquals(CappedNodeAgreementStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newCappedNodeAgreement("code-326", "title-60")
        try {
            item.moveTo(CappedNodeAgreementStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedNodeAgreementStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCappedNodeAgreement("code-326", "title-60")
        item.status = CappedNodeAgreementStatus.STOPPED
        assertFails("move") { item.moveTo(CappedNodeAgreementStatus.IDLE) }
    }
}
