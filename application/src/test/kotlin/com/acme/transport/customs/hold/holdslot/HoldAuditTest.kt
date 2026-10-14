package com.acme.transport.customs.hold.holdslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldAuditTest {
    @Test
    fun `starts in requested`() {
        val item = newHoldAudit("code-434", "name-276")
        assertEquals(HoldAuditStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newHoldAudit("code-434", "name-276")
        try {
            item.moveTo(HoldAuditStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldAuditStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newHoldAudit("code-434", "name-276")
        item.status = HoldAuditStatus.EXPIRED
        assertFails("move") { item.moveTo(HoldAuditStatus.REQUESTED) }
    }
}
