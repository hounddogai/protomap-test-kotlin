package com.acme.transport.fleetops.serviceplan.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanPreferenceTest {
    @Test
    fun `starts in idle`() {
        val item = newServicePlanPreference("code-168", "name-404")
        assertEquals(ServicePlanPreferenceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newServicePlanPreference("code-168", "name-404")
        try {
            item.moveTo(ServicePlanPreferenceStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanPreferenceStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newServicePlanPreference("code-168", "name-404")
        item.status = ServicePlanPreferenceStatus.STOPPED
        assertFails("move") { item.moveTo(ServicePlanPreferenceStatus.IDLE) }
    }
}
