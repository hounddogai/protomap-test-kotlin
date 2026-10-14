package com.acme.transport.network.servicearea.areaclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaPlanTest {
    @Test
    fun `starts in proposed`() {
        val item = newServiceAreaPlan("code-52", "title-246")
        assertEquals(ServiceAreaPlanStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newServiceAreaPlan("code-52", "title-246")
        try {
            item.moveTo(ServiceAreaPlanStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaPlanStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newServiceAreaPlan("code-52", "title-246")
        item.status = ServiceAreaPlanStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ServiceAreaPlanStatus.PROPOSED) }
    }
}
