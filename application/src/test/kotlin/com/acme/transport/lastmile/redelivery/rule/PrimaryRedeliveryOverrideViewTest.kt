package com.acme.transport.lastmile.redelivery.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryRedeliveryOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryRedeliveryOverride("code-735", "label-905")
        val view = toPrimaryRedeliveryOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryRedeliveryOverrideViews(mutableListOf<PrimaryRedeliveryOverride>(item, item)).size, "views")
    }
}
