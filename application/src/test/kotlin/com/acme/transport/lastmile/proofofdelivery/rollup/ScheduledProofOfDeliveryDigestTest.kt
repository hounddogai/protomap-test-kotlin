package com.acme.transport.lastmile.proofofdelivery.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledProofOfDeliveryDigestTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledProofOfDeliveryDigest("code-957", "title-618")
        assertEquals(ScheduledProofOfDeliveryDigestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newScheduledProofOfDeliveryDigest("code-957", "title-618")
        try {
            item.moveTo(ScheduledProofOfDeliveryDigestStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledProofOfDeliveryDigestStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledProofOfDeliveryDigest("code-957", "title-618")
        item.status = ScheduledProofOfDeliveryDigestStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledProofOfDeliveryDigestStatus.DRAFT) }
    }
}
