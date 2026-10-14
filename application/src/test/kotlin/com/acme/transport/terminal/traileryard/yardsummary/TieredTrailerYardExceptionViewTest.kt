package com.acme.transport.terminal.traileryard.yardsummary

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredTrailerYardExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredTrailerYardException("code-715", "label-390")
        val view = toTieredTrailerYardExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredTrailerYardExceptionViews(mutableListOf<TieredTrailerYardException>(item, item)).size, "views")
    }
}
