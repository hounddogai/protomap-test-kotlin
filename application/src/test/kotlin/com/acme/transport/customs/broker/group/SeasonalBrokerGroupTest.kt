package com.acme.transport.customs.broker.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalBrokerGroupTest {
    @Test
    fun `starts in draft`() {
        val item = newSeasonalBrokerGroup("code-182", "name-339")
        assertEquals(SeasonalBrokerGroupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newSeasonalBrokerGroup("code-182", "name-339")
        try {
            item.moveTo(SeasonalBrokerGroupStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalBrokerGroupStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSeasonalBrokerGroup("code-182", "name-339")
        item.status = SeasonalBrokerGroupStatus.CLOSED
        assertFails("move") { item.moveTo(SeasonalBrokerGroupStatus.DRAFT) }
    }
}
