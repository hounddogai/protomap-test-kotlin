package com.acme.transport.terminal.dwell.dwelltemplate

import kotlin.test.Test
import kotlin.test.assertEquals

class DwellExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDwellException("code-571", "name-25")
        val view = toDwellExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDwellExceptionViews(mutableListOf<DwellException>(item, item)).size, "views")
    }
}
