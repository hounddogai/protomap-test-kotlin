package com.acme.transport.lastmile.redelivery.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryQuotaTest {
    @Test
    fun `starts in proposed`() {
        val item = newRedeliveryQuota("code-580", "label-228")
        assertEquals(RedeliveryQuotaStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRedeliveryQuota("code-580", "label-228")
        try {
            item.moveTo(RedeliveryQuotaStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryQuotaStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRedeliveryQuota("code-580", "label-228")
        item.status = RedeliveryQuotaStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RedeliveryQuotaStatus.PROPOSED) }
    }
}
