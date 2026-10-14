package com.acme.transport.terminal.traileryard.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictTrailerYardAuditTest {
    @Test
    fun `starts in proposed`() {
        val item = newStrictTrailerYardAudit("code-883", "title-15")
        assertEquals(StrictTrailerYardAuditStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newStrictTrailerYardAudit("code-883", "title-15")
        try {
            item.moveTo(StrictTrailerYardAuditStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictTrailerYardAuditStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newStrictTrailerYardAudit("code-883", "title-15")
        item.status = StrictTrailerYardAuditStatus.WITHDRAWN
        assertFails("move") { item.moveTo(StrictTrailerYardAuditStatus.PROPOSED) }
    }
}
