package com.acme.transport.lastmile.attempt.template

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedAttemptExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedAttemptException("code-836", "name-870")
        val view = toCappedAttemptExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedAttemptExceptionViews(mutableListOf<CappedAttemptException>(item, item)).size, "views")
    }
}
