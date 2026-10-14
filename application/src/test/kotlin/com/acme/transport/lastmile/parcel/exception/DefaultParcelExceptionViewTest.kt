package com.acme.transport.lastmile.parcel.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultParcelExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultParcelException("code-300", "label-641")
        val view = toDefaultParcelExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultParcelExceptionViews(mutableListOf<DefaultParcelException>(item, item)).size, "views")
    }
}
