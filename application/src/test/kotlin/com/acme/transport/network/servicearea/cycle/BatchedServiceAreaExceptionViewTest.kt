package com.acme.transport.network.servicearea.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedServiceAreaExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedServiceAreaException("code-486", "title-570")
        val view = toBatchedServiceAreaExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedServiceAreaExceptionViews(mutableListOf<BatchedServiceAreaException>(item, item)).size, "views")
    }
}
