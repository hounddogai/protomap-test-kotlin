package com.acme.transport.customs.clearance.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackClearanceQuotaTest {
    @Test
    fun `starts in requested`() {
        val item = newFallbackClearanceQuota("code-520", "label-369")
        assertEquals(FallbackClearanceQuotaStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newFallbackClearanceQuota("code-520", "label-369")
        try {
            item.moveTo(FallbackClearanceQuotaStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackClearanceQuotaStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFallbackClearanceQuota("code-520", "label-369")
        item.status = FallbackClearanceQuotaStatus.EXPIRED
        assertFails("move") { item.moveTo(FallbackClearanceQuotaStatus.REQUESTED) }
    }
}
