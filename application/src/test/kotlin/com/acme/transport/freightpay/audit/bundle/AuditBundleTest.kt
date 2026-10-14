package com.acme.transport.freightpay.audit.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditBundleTest {
    @Test
    fun `starts in idle`() {
        val item = newAuditBundle("code-845", "label-270")
        assertEquals(AuditBundleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newAuditBundle("code-845", "label-270")
        try {
            item.moveTo(AuditBundleStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditBundleStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAuditBundle("code-845", "label-270")
        item.status = AuditBundleStatus.STOPPED
        assertFails("move") { item.moveTo(AuditBundleStatus.IDLE) }
    }
}
