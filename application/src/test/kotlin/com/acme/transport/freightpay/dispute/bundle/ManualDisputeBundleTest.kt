package com.acme.transport.freightpay.dispute.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDisputeBundleTest {
    @Test
    fun `starts in open`() {
        val item = newManualDisputeBundle("code-509", "label-303")
        assertEquals(ManualDisputeBundleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newManualDisputeBundle("code-509", "label-303")
        try {
            item.moveTo(ManualDisputeBundleStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDisputeBundleStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newManualDisputeBundle("code-509", "label-303")
        item.status = ManualDisputeBundleStatus.ARCHIVED
        assertFails("move") { item.moveTo(ManualDisputeBundleStatus.OPEN) }
    }
}
