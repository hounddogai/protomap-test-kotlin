package com.acme.transport.freightpay.audit.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditSegmentTest {
    @Test
    fun `starts in created`() {
        val item = newAuditSegment("code-87", "title-857")
        assertEquals(AuditSegmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newAuditSegment("code-87", "title-857")
        try {
            item.moveTo(AuditSegmentStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditSegmentStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAuditSegment("code-87", "title-857")
        item.status = AuditSegmentStatus.RETIRED
        assertFails("move") { item.moveTo(AuditSegmentStatus.CREATED) }
    }
}
