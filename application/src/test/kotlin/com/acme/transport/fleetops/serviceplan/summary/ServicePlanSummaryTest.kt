package com.acme.transport.fleetops.serviceplan.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanSummaryTest {
    @Test
    fun `starts in idle`() {
        val item = newServicePlanSummary("code-192", "name-787")
        assertEquals(ServicePlanSummaryStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newServicePlanSummary("code-192", "name-787")
        try {
            item.moveTo(ServicePlanSummaryStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanSummaryStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newServicePlanSummary("code-192", "name-787")
        item.status = ServicePlanSummaryStatus.STOPPED
        assertFails("move") { item.moveTo(ServicePlanSummaryStatus.IDLE) }
    }
}
