package com.acme.transport.fleetops.serviceplan.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedServicePlanRollupTest {
    @Test
    fun `starts in created`() {
        val item = newCappedServicePlanRollup("code-411", "name-330")
        assertEquals(CappedServicePlanRollupStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCappedServicePlanRollup("code-411", "name-330")
        try {
            item.moveTo(CappedServicePlanRollupStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedServicePlanRollupStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCappedServicePlanRollup("code-411", "name-330")
        item.status = CappedServicePlanRollupStatus.RETIRED
        assertFails("move") { item.moveTo(CappedServicePlanRollupStatus.CREATED) }
    }
}
