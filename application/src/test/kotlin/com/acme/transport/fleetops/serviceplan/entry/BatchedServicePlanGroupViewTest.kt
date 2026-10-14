package com.acme.transport.fleetops.serviceplan.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedServicePlanGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedServicePlanGroup("code-958", "name-201")
        val view = toBatchedServicePlanGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedServicePlanGroupViews(mutableListOf<BatchedServicePlanGroup>(item, item)).size, "views")
    }
}
