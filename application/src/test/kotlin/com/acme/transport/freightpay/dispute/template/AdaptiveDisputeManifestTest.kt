package com.acme.transport.freightpay.dispute.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveDisputeManifestTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveDisputeManifest("code-71", "name-458")
        assertEquals(AdaptiveDisputeManifestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newAdaptiveDisputeManifest("code-71", "name-458")
        try {
            item.moveTo(AdaptiveDisputeManifestStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveDisputeManifestStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveDisputeManifest("code-71", "name-458")
        item.status = AdaptiveDisputeManifestStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveDisputeManifestStatus.NEW) }
    }
}
