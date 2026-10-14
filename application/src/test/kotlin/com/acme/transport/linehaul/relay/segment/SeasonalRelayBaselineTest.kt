package com.acme.transport.linehaul.relay.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalRelayBaselineTest {
    @Test
    fun `starts in new`() {
        val item = newSeasonalRelayBaseline("code-558", "name-698")
        assertEquals(SeasonalRelayBaselineStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newSeasonalRelayBaseline("code-558", "name-698")
        try {
            item.moveTo(SeasonalRelayBaselineStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalRelayBaselineStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSeasonalRelayBaseline("code-558", "name-698")
        item.status = SeasonalRelayBaselineStatus.FAILED
        assertFails("move") { item.moveTo(SeasonalRelayBaselineStatus.NEW) }
    }
}
