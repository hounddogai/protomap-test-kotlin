package com.acme.transport.fleetops.serviceplan.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanManifestTest {
    @Test
    fun `starts in draft`() {
        val item = newServicePlanManifest("code-443", "name-921")
        assertEquals(ServicePlanManifestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newServicePlanManifest("code-443", "name-921")
        try {
            item.moveTo(ServicePlanManifestStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanManifestStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newServicePlanManifest("code-443", "name-921")
        item.status = ServicePlanManifestStatus.CLOSED
        assertFails("move") { item.moveTo(ServicePlanManifestStatus.DRAFT) }
    }
}
