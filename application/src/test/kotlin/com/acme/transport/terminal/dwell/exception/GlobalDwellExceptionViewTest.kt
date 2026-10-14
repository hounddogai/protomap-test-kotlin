package com.acme.transport.terminal.dwell.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalDwellExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalDwellException("code-576", "title-456")
        val view = toGlobalDwellExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalDwellExceptionViews(mutableListOf<GlobalDwellException>(item, item)).size, "views")
    }
}
