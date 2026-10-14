package com.acme.transport.fleetops.serviceplan.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanRollupTest {
    @Test
    fun `starts in created`() {
        val item = newServicePlanRollup("code-844", "name-821")
        assertEquals(ServicePlanRollupStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newServicePlanRollup("code-844", "name-821")
        try {
            item.moveTo(ServicePlanRollupStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanRollupStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newServicePlanRollup("code-844", "name-821")
        item.status = ServicePlanRollupStatus.RETIRED
        assertFails("move") { item.moveTo(ServicePlanRollupStatus.CREATED) }
    }
}
