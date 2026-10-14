package com.acme.transport.network.transittime.timeoverride

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultTransitTimeExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultTransitTimeException("code-41", "name-835")
        val view = toDefaultTransitTimeExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultTransitTimeExceptionViews(mutableListOf<DefaultTransitTimeException>(item, item)).size, "views")
    }
}
