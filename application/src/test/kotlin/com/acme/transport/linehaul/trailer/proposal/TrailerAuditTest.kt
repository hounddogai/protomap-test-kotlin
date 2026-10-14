package com.acme.transport.linehaul.trailer.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerAuditTest {
    @Test
    fun `starts in draft`() {
        val item = newTrailerAudit("code-926", "name-764")
        assertEquals(TrailerAuditStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newTrailerAudit("code-926", "name-764")
        try {
            item.moveTo(TrailerAuditStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerAuditStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTrailerAudit("code-926", "name-764")
        item.status = TrailerAuditStatus.CLOSED
        assertFails("move") { item.moveTo(TrailerAuditStatus.DRAFT) }
    }
}
