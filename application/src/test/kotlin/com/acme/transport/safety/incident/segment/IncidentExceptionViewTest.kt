package com.acme.transport.safety.incident.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class IncidentExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIncidentException("code-986", "label-16")
        val view = toIncidentExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIncidentExceptionViews(mutableListOf<IncidentException>(item, item)).size, "views")
    }
}
