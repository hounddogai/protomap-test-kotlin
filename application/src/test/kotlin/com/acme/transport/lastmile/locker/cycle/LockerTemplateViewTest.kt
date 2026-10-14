package com.acme.transport.lastmile.locker.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class LockerTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLockerTemplate("code-541", "name-825")
        val view = toLockerTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLockerTemplateViews(mutableListOf<LockerTemplate>(item, item)).size, "views")
    }
}
