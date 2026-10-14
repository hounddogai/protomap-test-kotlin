package com.acme.transport.freightpay.audit.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultAuditBaselineTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultAuditBaseline("code-326", "title-25")
        assertEquals(DefaultAuditBaselineStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newDefaultAuditBaseline("code-326", "title-25")
        try {
            item.moveTo(DefaultAuditBaselineStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultAuditBaselineStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultAuditBaseline("code-326", "title-25")
        item.status = DefaultAuditBaselineStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultAuditBaselineStatus.IDLE) }
    }
}
