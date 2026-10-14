package com.acme.transport.customs.declaration.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalDeclarationAuditTest {
    @Test
    fun `starts in new`() {
        val item = newGlobalDeclarationAudit("code-698", "name-105")
        assertEquals(GlobalDeclarationAuditStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newGlobalDeclarationAudit("code-698", "name-105")
        try {
            item.moveTo(GlobalDeclarationAuditStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalDeclarationAuditStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newGlobalDeclarationAudit("code-698", "name-105")
        item.status = GlobalDeclarationAuditStatus.FAILED
        assertFails("move") { item.moveTo(GlobalDeclarationAuditStatus.NEW) }
    }
}
