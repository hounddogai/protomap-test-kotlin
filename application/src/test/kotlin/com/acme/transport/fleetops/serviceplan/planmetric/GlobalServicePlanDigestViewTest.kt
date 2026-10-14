package com.acme.transport.fleetops.serviceplan.planmetric

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalServicePlanDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalServicePlanDigest("code-102", "title-930")
        val view = toGlobalServicePlanDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalServicePlanDigestViews(mutableListOf<GlobalServicePlanDigest>(item, item)).size, "views")
    }
}
