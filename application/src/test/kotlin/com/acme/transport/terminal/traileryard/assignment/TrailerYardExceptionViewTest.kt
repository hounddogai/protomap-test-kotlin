package com.acme.transport.terminal.traileryard.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerYardExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerYardException("code-332", "label-898")
        val view = toTrailerYardExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerYardExceptionViews(mutableListOf<TrailerYardException>(item, item)).size, "views")
    }
}
