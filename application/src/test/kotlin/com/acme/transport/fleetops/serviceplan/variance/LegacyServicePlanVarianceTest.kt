package com.acme.transport.fleetops.serviceplan.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyServicePlanVarianceTest {
    @Test
    fun `starts in created`() {
        val item = newLegacyServicePlanVariance("code-286", "name-955")
        assertEquals(LegacyServicePlanVarianceStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newLegacyServicePlanVariance("code-286", "name-955")
        try {
            item.moveTo(LegacyServicePlanVarianceStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyServicePlanVarianceStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLegacyServicePlanVariance("code-286", "name-955")
        item.status = LegacyServicePlanVarianceStatus.RETIRED
        assertFails("move") { item.moveTo(LegacyServicePlanVarianceStatus.CREATED) }
    }
}
