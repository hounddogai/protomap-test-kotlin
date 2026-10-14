package com.acme.transport.freightpay.audit.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredAuditManifestTest {
    @Test
    fun `starts in idle`() {
        val item = newTieredAuditManifest("code-247", "title-244")
        assertEquals(TieredAuditManifestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newTieredAuditManifest("code-247", "title-244")
        try {
            item.moveTo(TieredAuditManifestStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredAuditManifestStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTieredAuditManifest("code-247", "title-244")
        item.status = TieredAuditManifestStatus.STOPPED
        assertFails("move") { item.moveTo(TieredAuditManifestStatus.IDLE) }
    }
}
