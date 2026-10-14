package com.acme.transport.customs.declaration.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class DeclarationScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDeclarationSchedule("code-507", "name-392")
        val view = toDeclarationScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDeclarationScheduleViews(mutableListOf<DeclarationSchedule>(item, item)).size, "views")
    }
}
