package com.acme.transport.freightpay.audit.legacyauditpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedAuditDigestTest {
    @Test
    fun `starts in idle`() {
        val item = newCappedAuditDigest("code-270", "name-986")
        assertEquals(CappedAuditDigestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newCappedAuditDigest("code-270", "name-986")
        try {
            item.moveTo(CappedAuditDigestStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedAuditDigestStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCappedAuditDigest("code-270", "name-986")
        item.status = CappedAuditDigestStatus.STOPPED
        assertFails("move") { item.moveTo(CappedAuditDigestStatus.IDLE) }
    }
}
