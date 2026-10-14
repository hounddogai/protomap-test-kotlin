package com.acme.transport.linehaul.backhaul.backhaulaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientBackhaulAuditTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientBackhaulAudit("code-151", "name-45")
        assertEquals(LenientBackhaulAuditStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newLenientBackhaulAudit("code-151", "name-45")
        try {
            item.moveTo(LenientBackhaulAuditStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientBackhaulAuditStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientBackhaulAudit("code-151", "name-45")
        item.status = LenientBackhaulAuditStatus.CLOSED
        assertFails("move") { item.moveTo(LenientBackhaulAuditStatus.DRAFT) }
    }
}
