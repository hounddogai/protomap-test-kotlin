package com.acme.transport.lastmile.drop.dropreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDropAuditTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictDropAudit("code-712", "title-517")
        assertEquals(StrictDropAuditStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newStrictDropAudit("code-712", "title-517")
        try {
            item.moveTo(StrictDropAuditStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDropAuditStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictDropAudit("code-712", "title-517")
        item.status = StrictDropAuditStatus.CLOSED
        assertFails("move") { item.moveTo(StrictDropAuditStatus.DRAFT) }
    }
}
