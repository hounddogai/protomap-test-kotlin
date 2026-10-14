package com.acme.transport.network.zone.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyZoneScoreTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyZoneScore("code-626", "title-46")
        assertEquals(LegacyZoneScoreStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newLegacyZoneScore("code-626", "title-46")
        try {
            item.moveTo(LegacyZoneScoreStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyZoneScoreStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyZoneScore("code-626", "title-46")
        item.status = LegacyZoneScoreStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyZoneScoreStatus.PROPOSED) }
    }
}
