package com.acme.transport.network.cutoff.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultCutoffAuditTest {
    @Test
    fun `starts in open`() {
        val item = newDefaultCutoffAudit("code-423", "name-436")
        assertEquals(DefaultCutoffAuditStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newDefaultCutoffAudit("code-423", "name-436")
        try {
            item.moveTo(DefaultCutoffAuditStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultCutoffAuditStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDefaultCutoffAudit("code-423", "name-436")
        item.status = DefaultCutoffAuditStatus.ARCHIVED
        assertFails("move") { item.moveTo(DefaultCutoffAuditStatus.OPEN) }
    }
}
