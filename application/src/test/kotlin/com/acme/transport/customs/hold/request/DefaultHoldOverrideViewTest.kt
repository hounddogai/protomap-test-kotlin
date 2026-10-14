package com.acme.transport.customs.hold.request

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultHoldOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultHoldOverride("code-244", "label-110")
        val view = toDefaultHoldOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultHoldOverrideViews(mutableListOf<DefaultHoldOverride>(item, item)).size, "views")
    }
}
