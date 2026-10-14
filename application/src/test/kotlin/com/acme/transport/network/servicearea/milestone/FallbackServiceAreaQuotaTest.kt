package com.acme.transport.network.servicearea.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackServiceAreaQuotaTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackServiceAreaQuota("code-575", "title-744")
        assertEquals(FallbackServiceAreaQuotaStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newFallbackServiceAreaQuota("code-575", "title-744")
        try {
            item.moveTo(FallbackServiceAreaQuotaStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackServiceAreaQuotaStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackServiceAreaQuota("code-575", "title-744")
        item.status = FallbackServiceAreaQuotaStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackServiceAreaQuotaStatus.OPEN) }
    }
}
