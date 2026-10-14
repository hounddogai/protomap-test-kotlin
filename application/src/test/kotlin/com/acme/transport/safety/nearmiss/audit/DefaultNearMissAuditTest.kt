package com.acme.transport.safety.nearmiss.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultNearMissAuditTest {
    @Test
    fun `starts in planned`() {
        val item = newDefaultNearMissAudit("code-850", "title-810")
        assertEquals(DefaultNearMissAuditStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newDefaultNearMissAudit("code-850", "title-810")
        try {
            item.moveTo(DefaultNearMissAuditStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultNearMissAuditStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDefaultNearMissAudit("code-850", "title-810")
        item.status = DefaultNearMissAuditStatus.CANCELLED
        assertFails("move") { item.moveTo(DefaultNearMissAuditStatus.PLANNED) }
    }
}
