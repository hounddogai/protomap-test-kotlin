package com.acme.transport.customs.broker.lenientbrokerestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalBrokerPlanTest {
    @Test
    fun `starts in planned`() {
        val item = newSeasonalBrokerPlan("code-498", "name-471")
        assertEquals(SeasonalBrokerPlanStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newSeasonalBrokerPlan("code-498", "name-471")
        try {
            item.moveTo(SeasonalBrokerPlanStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalBrokerPlanStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSeasonalBrokerPlan("code-498", "name-471")
        item.status = SeasonalBrokerPlanStatus.CANCELLED
        assertFails("move") { item.moveTo(SeasonalBrokerPlanStatus.PLANNED) }
    }
}
