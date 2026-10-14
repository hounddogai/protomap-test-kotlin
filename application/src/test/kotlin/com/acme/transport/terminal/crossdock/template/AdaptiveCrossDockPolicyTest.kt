package com.acme.transport.terminal.crossdock.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveCrossDockPolicyTest {
    @Test
    fun `starts in proposed`() {
        val item = newAdaptiveCrossDockPolicy("code-996", "name-821")
        assertEquals(AdaptiveCrossDockPolicyStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newAdaptiveCrossDockPolicy("code-996", "name-821")
        try {
            item.moveTo(AdaptiveCrossDockPolicyStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveCrossDockPolicyStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAdaptiveCrossDockPolicy("code-996", "name-821")
        item.status = AdaptiveCrossDockPolicyStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AdaptiveCrossDockPolicyStatus.PROPOSED) }
    }
}
