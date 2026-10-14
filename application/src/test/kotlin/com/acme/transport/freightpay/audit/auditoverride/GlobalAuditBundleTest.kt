package com.acme.transport.freightpay.audit.auditoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalAuditBundleTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalAuditBundle("code-522", "title-969")
        assertEquals(GlobalAuditBundleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newGlobalAuditBundle("code-522", "title-969")
        try {
            item.moveTo(GlobalAuditBundleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalAuditBundleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalAuditBundle("code-522", "title-969")
        item.status = GlobalAuditBundleStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalAuditBundleStatus.REQUESTED) }
    }
}
