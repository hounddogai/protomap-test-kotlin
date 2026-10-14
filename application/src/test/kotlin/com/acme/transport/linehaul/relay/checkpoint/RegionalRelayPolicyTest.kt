package com.acme.transport.linehaul.relay.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalRelayPolicyTest {
    @Test
    fun `starts in idle`() {
        val item = newRegionalRelayPolicy("code-495", "name-160")
        assertEquals(RegionalRelayPolicyStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRegionalRelayPolicy("code-495", "name-160")
        try {
            item.moveTo(RegionalRelayPolicyStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalRelayPolicyStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRegionalRelayPolicy("code-495", "name-160")
        item.status = RegionalRelayPolicyStatus.STOPPED
        assertFails("move") { item.moveTo(RegionalRelayPolicyStatus.IDLE) }
    }
}
