package com.acme.transport.customs.broker.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyBrokerPlanTest {
    @Test
    fun `starts in draft`() {
        val item = newLegacyBrokerPlan("code-876", "name-942")
        assertEquals(LegacyBrokerPlanStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newLegacyBrokerPlan("code-876", "name-942")
        try {
            item.moveTo(LegacyBrokerPlanStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyBrokerPlanStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLegacyBrokerPlan("code-876", "name-942")
        item.status = LegacyBrokerPlanStatus.CLOSED
        assertFails("move") { item.moveTo(LegacyBrokerPlanStatus.DRAFT) }
    }
}
