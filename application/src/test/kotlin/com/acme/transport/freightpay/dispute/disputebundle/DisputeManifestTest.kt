package com.acme.transport.freightpay.dispute.disputebundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeManifestTest {
    @Test
    fun `starts in idle`() {
        val item = newDisputeManifest("code-421", "title-861")
        assertEquals(DisputeManifestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDisputeManifest("code-421", "title-861")
        try {
            item.moveTo(DisputeManifestStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeManifestStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDisputeManifest("code-421", "title-861")
        item.status = DisputeManifestStatus.STOPPED
        assertFails("move") { item.moveTo(DisputeManifestStatus.IDLE) }
    }
}
