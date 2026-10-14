package com.acme.transport.lastmile.locker.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class LockerExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLockerException("code-479", "name-401")
        val view = toLockerExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLockerExceptionViews(mutableListOf<LockerException>(item, item)).size, "views")
    }
}
