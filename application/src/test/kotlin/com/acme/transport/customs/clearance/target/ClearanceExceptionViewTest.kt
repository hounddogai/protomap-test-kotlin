package com.acme.transport.customs.clearance.target

import kotlin.test.Test
import kotlin.test.assertEquals

class ClearanceExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newClearanceException("code-260", "name-910")
        val view = toClearanceExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toClearanceExceptionViews(mutableListOf<ClearanceException>(item, item)).size, "views")
    }
}
