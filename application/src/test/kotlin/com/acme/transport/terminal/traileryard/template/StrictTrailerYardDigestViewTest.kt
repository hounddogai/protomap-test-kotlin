package com.acme.transport.terminal.traileryard.template

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictTrailerYardDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictTrailerYardDigest("code-633", "name-961")
        val view = toStrictTrailerYardDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictTrailerYardDigestViews(mutableListOf<StrictTrailerYardDigest>(item, item)).size, "views")
    }
}
