package com.acme.transport.freightpay.audit.auditbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalAuditSegmentTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalAuditSegment("code-753", "label-174")
        assertEquals(GlobalAuditSegmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newGlobalAuditSegment("code-753", "label-174")
        try {
            item.moveTo(GlobalAuditSegmentStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalAuditSegmentStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalAuditSegment("code-753", "label-174")
        item.status = GlobalAuditSegmentStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalAuditSegmentStatus.REQUESTED) }
    }
}
