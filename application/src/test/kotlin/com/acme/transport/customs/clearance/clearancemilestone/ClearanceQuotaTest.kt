package com.acme.transport.customs.clearance.clearancemilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceQuotaTest {
    @Test
    fun `starts in planned`() {
        val item = newClearanceQuota("code-126", "title-320")
        assertEquals(ClearanceQuotaStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newClearanceQuota("code-126", "title-320")
        try {
            item.moveTo(ClearanceQuotaStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceQuotaStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newClearanceQuota("code-126", "title-320")
        item.status = ClearanceQuotaStatus.CANCELLED
        assertFails("move") { item.moveTo(ClearanceQuotaStatus.PLANNED) }
    }
}
