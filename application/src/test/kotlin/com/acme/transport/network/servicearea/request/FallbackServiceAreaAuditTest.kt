package com.acme.transport.network.servicearea.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackServiceAreaAuditTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackServiceAreaAudit("code-132", "title-244")
        assertEquals(FallbackServiceAreaAuditStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newFallbackServiceAreaAudit("code-132", "title-244")
        try {
            item.moveTo(FallbackServiceAreaAuditStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackServiceAreaAuditStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackServiceAreaAudit("code-132", "title-244")
        item.status = FallbackServiceAreaAuditStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackServiceAreaAuditStatus.OPEN) }
    }
}
