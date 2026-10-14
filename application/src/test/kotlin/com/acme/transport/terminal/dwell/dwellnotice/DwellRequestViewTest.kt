package com.acme.transport.terminal.dwell.dwellnotice

import kotlin.test.Test
import kotlin.test.assertEquals

class DwellRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDwellRequest("code-639", "title-921")
        val view = toDwellRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDwellRequestViews(mutableListOf<DwellRequest>(item, item)).size, "views")
    }
}
