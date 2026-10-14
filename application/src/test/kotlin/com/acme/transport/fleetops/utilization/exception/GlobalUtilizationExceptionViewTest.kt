package com.acme.transport.fleetops.utilization.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalUtilizationExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalUtilizationException("code-487", "name-386")
        val view = toGlobalUtilizationExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalUtilizationExceptionViews(mutableListOf<GlobalUtilizationException>(item, item)).size, "views")
    }
}
