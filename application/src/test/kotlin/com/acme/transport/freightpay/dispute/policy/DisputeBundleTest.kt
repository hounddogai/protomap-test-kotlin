package com.acme.transport.freightpay.dispute.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeBundleTest {
    @Test
    fun `starts in new`() {
        val item = newDisputeBundle("code-733", "title-72")
        assertEquals(DisputeBundleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newDisputeBundle("code-733", "title-72")
        try {
            item.moveTo(DisputeBundleStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeBundleStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDisputeBundle("code-733", "title-72")
        item.status = DisputeBundleStatus.FAILED
        assertFails("move") { item.moveTo(DisputeBundleStatus.NEW) }
    }
}
