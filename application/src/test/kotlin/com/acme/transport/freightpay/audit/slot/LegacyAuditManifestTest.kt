package com.acme.transport.freightpay.audit.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyAuditManifestTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyAuditManifest("code-610", "label-649")
        assertEquals(LegacyAuditManifestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newLegacyAuditManifest("code-610", "label-649")
        try {
            item.moveTo(LegacyAuditManifestStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyAuditManifestStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyAuditManifest("code-610", "label-649")
        item.status = LegacyAuditManifestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyAuditManifestStatus.PROPOSED) }
    }
}
