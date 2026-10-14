package com.acme.transport.safety.incident.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryIncidentQuotaTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryIncidentQuota("code-608", "name-904")
        assertEquals(PrimaryIncidentQuotaStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newPrimaryIncidentQuota("code-608", "name-904")
        try {
            item.moveTo(PrimaryIncidentQuotaStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryIncidentQuotaStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryIncidentQuota("code-608", "name-904")
        item.status = PrimaryIncidentQuotaStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryIncidentQuotaStatus.OPEN) }
    }
}
