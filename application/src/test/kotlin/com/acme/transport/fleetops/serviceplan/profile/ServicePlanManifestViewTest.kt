package com.acme.transport.fleetops.serviceplan.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class ServicePlanManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServicePlanManifest("code-443", "name-921")
        val view = toServicePlanManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServicePlanManifestViews(mutableListOf<ServicePlanManifest>(item, item)).size, "views")
    }
}
