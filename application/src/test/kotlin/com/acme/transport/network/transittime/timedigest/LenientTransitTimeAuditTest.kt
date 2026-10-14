package com.acme.transport.network.transittime.timedigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientTransitTimeAuditTest {
    @Test
    fun `starts in open`() {
        val item = newLenientTransitTimeAudit("code-850", "title-934")
        assertEquals(LenientTransitTimeAuditStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newLenientTransitTimeAudit("code-850", "title-934")
        try {
            item.moveTo(LenientTransitTimeAuditStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientTransitTimeAuditStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientTransitTimeAudit("code-850", "title-934")
        item.status = LenientTransitTimeAuditStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientTransitTimeAuditStatus.OPEN) }
    }
}
