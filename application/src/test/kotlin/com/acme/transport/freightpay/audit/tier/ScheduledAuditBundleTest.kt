package com.acme.transport.freightpay.audit.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledAuditBundleTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledAuditBundle("code-268", "name-981")
        assertEquals(ScheduledAuditBundleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newScheduledAuditBundle("code-268", "name-981")
        try {
            item.moveTo(ScheduledAuditBundleStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledAuditBundleStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledAuditBundle("code-268", "name-981")
        item.status = ScheduledAuditBundleStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledAuditBundleStatus.DRAFT) }
    }
}
