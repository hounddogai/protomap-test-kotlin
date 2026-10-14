package com.acme.transport.customs.declaration.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDeclarationAuditTest {
    @Test
    fun `starts in planned`() {
        val item = newRegionalDeclarationAudit("code-15", "label-92")
        assertEquals(RegionalDeclarationAuditStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRegionalDeclarationAudit("code-15", "label-92")
        try {
            item.moveTo(RegionalDeclarationAuditStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDeclarationAuditStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRegionalDeclarationAudit("code-15", "label-92")
        item.status = RegionalDeclarationAuditStatus.CANCELLED
        assertFails("move") { item.moveTo(RegionalDeclarationAuditStatus.PLANNED) }
    }
}
