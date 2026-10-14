package com.acme.transport.terminal.traileryard.yardquota

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedTrailerYardQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedTrailerYardQuota("code-215", "title-997")
        val view = toBatchedTrailerYardQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedTrailerYardQuotaViews(mutableListOf<BatchedTrailerYardQuota>(item, item)).size, "views")
    }
}
