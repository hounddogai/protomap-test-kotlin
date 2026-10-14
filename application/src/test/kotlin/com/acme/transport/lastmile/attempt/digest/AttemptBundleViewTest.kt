package com.acme.transport.lastmile.attempt.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class AttemptBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAttemptBundle("code-72", "name-532")
        val view = toAttemptBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAttemptBundleViews(mutableListOf<AttemptBundle>(item, item)).size, "views")
    }
}
