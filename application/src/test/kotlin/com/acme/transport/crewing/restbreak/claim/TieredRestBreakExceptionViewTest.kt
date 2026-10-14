package com.acme.transport.crewing.restbreak.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredRestBreakExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredRestBreakException("code-378", "label-763")
        val view = toTieredRestBreakExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredRestBreakExceptionViews(mutableListOf<TieredRestBreakException>(item, item)).size, "views")
    }
}
