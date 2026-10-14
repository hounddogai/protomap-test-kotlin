package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedProofOfDeliveryNoticeTest {
    @Test
    fun `starts in planned`() {
        val item = newCappedProofOfDeliveryNotice("code-946", "name-788")
        assertEquals(CappedProofOfDeliveryNoticeStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newCappedProofOfDeliveryNotice("code-946", "name-788")
        try {
            item.moveTo(CappedProofOfDeliveryNoticeStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedProofOfDeliveryNoticeStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCappedProofOfDeliveryNotice("code-946", "name-788")
        item.status = CappedProofOfDeliveryNoticeStatus.CANCELLED
        assertFails("move") { item.moveTo(CappedProofOfDeliveryNoticeStatus.PLANNED) }
    }
}
