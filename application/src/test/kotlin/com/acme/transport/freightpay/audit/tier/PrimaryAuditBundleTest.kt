package com.acme.transport.freightpay.audit.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryAuditBundleTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryAuditBundle("code-608", "title-842")
        assertEquals(PrimaryAuditBundleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newPrimaryAuditBundle("code-608", "title-842")
        try {
            item.moveTo(PrimaryAuditBundleStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryAuditBundleStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryAuditBundle("code-608", "title-842")
        item.status = PrimaryAuditBundleStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryAuditBundleStatus.NEW) }
    }
}
