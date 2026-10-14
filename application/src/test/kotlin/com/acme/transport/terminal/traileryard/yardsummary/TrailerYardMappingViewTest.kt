package com.acme.transport.terminal.traileryard.yardsummary

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerYardMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerYardMapping("code-684", "name-69")
        val view = toTrailerYardMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerYardMappingViews(mutableListOf<TrailerYardMapping>(item, item)).size, "views")
    }
}
