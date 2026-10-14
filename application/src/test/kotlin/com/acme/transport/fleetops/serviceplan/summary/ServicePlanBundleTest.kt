package com.acme.transport.fleetops.serviceplan.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanBundleTest {
    @Test
    fun `starts in new`() {
        val item = newServicePlanBundle("code-601", "name-712")
        assertEquals(ServicePlanBundleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newServicePlanBundle("code-601", "name-712")
        try {
            item.moveTo(ServicePlanBundleStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanBundleStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newServicePlanBundle("code-601", "name-712")
        item.status = ServicePlanBundleStatus.FAILED
        assertFails("move") { item.moveTo(ServicePlanBundleStatus.NEW) }
    }
}
