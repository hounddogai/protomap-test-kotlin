package com.acme.transport.linehaul.relay.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingRelayScoreTest {
    @Test
    fun `starts in created`() {
        val item = newRollingRelayScore("code-272", "name-991")
        assertEquals(RollingRelayScoreStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRollingRelayScore("code-272", "name-991")
        try {
            item.moveTo(RollingRelayScoreStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingRelayScoreStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingRelayScore("code-272", "name-991")
        item.status = RollingRelayScoreStatus.RETIRED
        assertFails("move") { item.moveTo(RollingRelayScoreStatus.CREATED) }
    }
}
