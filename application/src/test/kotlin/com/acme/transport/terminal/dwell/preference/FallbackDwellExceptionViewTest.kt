package com.acme.transport.terminal.dwell.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackDwellExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackDwellException("code-7", "name-1")
        val view = toFallbackDwellExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackDwellExceptionViews(mutableListOf<FallbackDwellException>(item, item)).size, "views")
    }
}
