package com.acme.transport.fleetops.utilization.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationQuotaTest {
    @Test
    fun `starts in draft`() {
        val item = newUtilizationQuota("code-697", "label-279")
        assertEquals(UtilizationQuotaStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newUtilizationQuota("code-697", "label-279")
        try {
            item.moveTo(UtilizationQuotaStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationQuotaStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newUtilizationQuota("code-697", "label-279")
        item.status = UtilizationQuotaStatus.CLOSED
        assertFails("move") { item.moveTo(UtilizationQuotaStatus.DRAFT) }
    }
}
