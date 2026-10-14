package com.acme.transport.customs.broker.brokergroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalBrokerOverrideTest {
    @Test
    fun `starts in created`() {
        val item = newSeasonalBrokerOverride("code-789", "name-218")
        assertEquals(SeasonalBrokerOverrideStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newSeasonalBrokerOverride("code-789", "name-218")
        try {
            item.moveTo(SeasonalBrokerOverrideStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalBrokerOverrideStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSeasonalBrokerOverride("code-789", "name-218")
        item.status = SeasonalBrokerOverrideStatus.RETIRED
        assertFails("move") { item.moveTo(SeasonalBrokerOverrideStatus.CREATED) }
    }
}
