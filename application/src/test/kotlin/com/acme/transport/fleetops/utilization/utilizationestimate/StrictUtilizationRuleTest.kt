package com.acme.transport.fleetops.utilization.utilizationestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictUtilizationRuleTest {
    @Test
    fun `starts in requested`() {
        val item = newStrictUtilizationRule("code-540", "title-975")
        assertEquals(StrictUtilizationRuleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newStrictUtilizationRule("code-540", "title-975")
        try {
            item.moveTo(StrictUtilizationRuleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictUtilizationRuleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newStrictUtilizationRule("code-540", "title-975")
        item.status = StrictUtilizationRuleStatus.EXPIRED
        assertFails("move") { item.moveTo(StrictUtilizationRuleStatus.REQUESTED) }
    }
}
