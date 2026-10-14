package com.acme.transport.linehaul.relay.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveRelayDigestTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveRelayDigest("code-649", "label-627")
        assertEquals(AdaptiveRelayDigestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newAdaptiveRelayDigest("code-649", "label-627")
        try {
            item.moveTo(AdaptiveRelayDigestStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveRelayDigestStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveRelayDigest("code-649", "label-627")
        item.status = AdaptiveRelayDigestStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveRelayDigestStatus.NEW) }
    }
}
