package com.acme.transport.lastmile.locker.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultLockerAuditTest {
    @Test
    fun `starts in open`() {
        val item = newDefaultLockerAudit("code-368", "name-926")
        assertEquals(DefaultLockerAuditStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newDefaultLockerAudit("code-368", "name-926")
        try {
            item.moveTo(DefaultLockerAuditStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultLockerAuditStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDefaultLockerAudit("code-368", "name-926")
        item.status = DefaultLockerAuditStatus.ARCHIVED
        assertFails("move") { item.moveTo(DefaultLockerAuditStatus.OPEN) }
    }
}
