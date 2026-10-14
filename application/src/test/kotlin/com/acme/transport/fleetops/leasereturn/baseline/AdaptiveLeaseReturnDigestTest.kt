package com.acme.transport.fleetops.leasereturn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveLeaseReturnDigestTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveLeaseReturnDigest("code-994", "title-853")
        assertEquals(AdaptiveLeaseReturnDigestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newAdaptiveLeaseReturnDigest("code-994", "title-853")
        try {
            item.moveTo(AdaptiveLeaseReturnDigestStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveLeaseReturnDigestStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveLeaseReturnDigest("code-994", "title-853")
        item.status = AdaptiveLeaseReturnDigestStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveLeaseReturnDigestStatus.IDLE) }
    }
}
