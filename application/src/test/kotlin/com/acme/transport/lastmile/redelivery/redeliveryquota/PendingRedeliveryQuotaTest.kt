package com.acme.transport.lastmile.redelivery.redeliveryquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingRedeliveryQuotaTest {
    @Test
    fun `starts in draft`() {
        val item = newPendingRedeliveryQuota("code-63", "title-548")
        assertEquals(PendingRedeliveryQuotaStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newPendingRedeliveryQuota("code-63", "title-548")
        try {
            item.moveTo(PendingRedeliveryQuotaStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingRedeliveryQuotaStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPendingRedeliveryQuota("code-63", "title-548")
        item.status = PendingRedeliveryQuotaStatus.CLOSED
        assertFails("move") { item.moveTo(PendingRedeliveryQuotaStatus.DRAFT) }
    }
}
