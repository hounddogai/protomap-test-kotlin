package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackAttemptExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackAttemptException("code-213", "label-40")
        val view = toFallbackAttemptExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackAttemptExceptionViews(mutableListOf<FallbackAttemptException>(item, item)).size, "views")
    }
}
