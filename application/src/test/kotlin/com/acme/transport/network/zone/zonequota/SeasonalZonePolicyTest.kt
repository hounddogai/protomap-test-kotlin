package com.acme.transport.network.zone.zonequota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalZonePolicyTest {
    @Test
    fun `starts in created`() {
        val item = newSeasonalZonePolicy("code-646", "name-204")
        assertEquals(SeasonalZonePolicyStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newSeasonalZonePolicy("code-646", "name-204")
        try {
            item.moveTo(SeasonalZonePolicyStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalZonePolicyStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSeasonalZonePolicy("code-646", "name-204")
        item.status = SeasonalZonePolicyStatus.RETIRED
        assertFails("move") { item.moveTo(SeasonalZonePolicyStatus.CREATED) }
    }
}
