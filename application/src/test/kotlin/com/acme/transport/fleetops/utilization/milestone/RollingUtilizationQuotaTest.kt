package com.acme.transport.fleetops.utilization.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingUtilizationQuotaTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingUtilizationQuota("code-413", "name-979")
        assertEquals(RollingUtilizationQuotaStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newRollingUtilizationQuota("code-413", "name-979")
        try {
            item.moveTo(RollingUtilizationQuotaStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingUtilizationQuotaStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingUtilizationQuota("code-413", "name-979")
        item.status = RollingUtilizationQuotaStatus.CLOSED
        assertFails("move") { item.moveTo(RollingUtilizationQuotaStatus.DRAFT) }
    }
}
