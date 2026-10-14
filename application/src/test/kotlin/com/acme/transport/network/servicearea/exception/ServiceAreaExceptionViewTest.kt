package com.acme.transport.network.servicearea.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaException("code-885", "title-49")
        val view = toServiceAreaExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaExceptionViews(mutableListOf<ServiceAreaException>(item, item)).size, "views")
    }
}
