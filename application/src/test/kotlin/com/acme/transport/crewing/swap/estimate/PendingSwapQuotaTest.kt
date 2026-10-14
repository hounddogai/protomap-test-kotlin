package com.acme.transport.crewing.swap.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingSwapQuotaTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingSwapQuota("code-240", "label-445")
        assertEquals(PendingSwapQuotaStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newPendingSwapQuota("code-240", "label-445")
        try {
            item.moveTo(PendingSwapQuotaStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingSwapQuotaStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingSwapQuota("code-240", "label-445")
        item.status = PendingSwapQuotaStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingSwapQuotaStatus.PLANNED) }
    }
}
