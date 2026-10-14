package com.acme.transport.freightpay.accessorial.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultAccessorialRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultAccessorialRollup("code-281", "name-983")
        val view = toDefaultAccessorialRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultAccessorialRollupViews(mutableListOf<DefaultAccessorialRollup>(item, item)).size, "views")
    }
}
