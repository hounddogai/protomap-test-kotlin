package com.acme.transport.linehaul.relay.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredRelayOverrideTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredRelayOverride("code-990", "name-694")
        assertEquals(TieredRelayOverrideStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newTieredRelayOverride("code-990", "name-694")
        try {
            item.moveTo(TieredRelayOverrideStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredRelayOverrideStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredRelayOverride("code-990", "name-694")
        item.status = TieredRelayOverrideStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredRelayOverrideStatus.PROPOSED) }
    }
}
