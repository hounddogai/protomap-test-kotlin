package com.acme.transport.freightpay.audit.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveAuditManifestTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveAuditManifest("code-544", "name-547")
        assertEquals(AdaptiveAuditManifestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newAdaptiveAuditManifest("code-544", "name-547")
        try {
            item.moveTo(AdaptiveAuditManifestStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveAuditManifestStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveAuditManifest("code-544", "name-547")
        item.status = AdaptiveAuditManifestStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveAuditManifestStatus.IDLE) }
    }
}
