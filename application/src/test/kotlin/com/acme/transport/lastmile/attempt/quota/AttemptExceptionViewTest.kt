package com.acme.transport.lastmile.attempt.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class AttemptExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAttemptException("code-292", "title-207")
        val view = toAttemptExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAttemptExceptionViews(mutableListOf<AttemptException>(item, item)).size, "views")
    }
}
