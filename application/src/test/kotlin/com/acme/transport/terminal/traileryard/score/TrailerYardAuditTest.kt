package com.acme.transport.terminal.traileryard.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardAuditTest {
    @Test
    fun `starts in proposed`() {
        val item = newTrailerYardAudit("code-194", "name-734")
        assertEquals(TrailerYardAuditStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newTrailerYardAudit("code-194", "name-734")
        try {
            item.moveTo(TrailerYardAuditStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardAuditStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTrailerYardAudit("code-194", "name-734")
        item.status = TrailerYardAuditStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TrailerYardAuditStatus.PROPOSED) }
    }
}
