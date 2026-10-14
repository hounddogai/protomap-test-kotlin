package com.acme.transport.freightpay.audit.auditproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditManifestTest {
    @Test
    fun `starts in created`() {
        val item = newAuditManifest("code-664", "title-686")
        assertEquals(AuditManifestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newAuditManifest("code-664", "title-686")
        try {
            item.moveTo(AuditManifestStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditManifestStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAuditManifest("code-664", "title-686")
        item.status = AuditManifestStatus.RETIRED
        assertFails("move") { item.moveTo(AuditManifestStatus.CREATED) }
    }
}
