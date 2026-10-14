package com.acme.transport.crewing.restbreak.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class RestBreakExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRestBreakException("code-126", "label-376")
        val view = toRestBreakExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRestBreakExceptionViews(mutableListOf<RestBreakException>(item, item)).size, "views")
    }
}
