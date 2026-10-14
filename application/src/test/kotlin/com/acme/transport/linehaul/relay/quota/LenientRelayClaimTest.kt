package com.acme.transport.linehaul.relay.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientRelayClaimTest {
    @Test
    fun `starts in open`() {
        val item = newLenientRelayClaim("code-695", "label-327")
        assertEquals(LenientRelayClaimStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newLenientRelayClaim("code-695", "label-327")
        try {
            item.moveTo(LenientRelayClaimStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientRelayClaimStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientRelayClaim("code-695", "label-327")
        item.status = LenientRelayClaimStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientRelayClaimStatus.OPEN) }
    }
}
