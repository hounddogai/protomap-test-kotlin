package com.acme.transport.linehaul.relay.relaytarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalRelayTargetTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalRelayTarget("code-665", "title-842")
        assertEquals(SeasonalRelayTargetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newSeasonalRelayTarget("code-665", "title-842")
        try {
            item.moveTo(SeasonalRelayTargetStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalRelayTargetStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalRelayTarget("code-665", "title-842")
        item.status = SeasonalRelayTargetStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalRelayTargetStatus.REQUESTED) }
    }
}
