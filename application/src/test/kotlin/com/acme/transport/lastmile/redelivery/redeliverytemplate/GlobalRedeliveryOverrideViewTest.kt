package com.acme.transport.lastmile.redelivery.redeliverytemplate

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalRedeliveryOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalRedeliveryOverride("code-55", "label-15")
        val view = toGlobalRedeliveryOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalRedeliveryOverrideViews(mutableListOf<GlobalRedeliveryOverride>(item, item)).size, "views")
    }
}
