package com.acme.transport.linehaul.trailer.trailerpreference

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryTrailerNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryTrailerNotice("code-391", "title-320")
        val view = toPrimaryTrailerNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryTrailerNoticeViews(mutableListOf<PrimaryTrailerNotice>(item, item)).size, "views")
    }
}
