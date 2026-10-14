package com.acme.transport.lastmile.redelivery.redeliveryquota

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryRedeliveryPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryRedeliveryPolicy("code-351", "name-263")
        val view = toPrimaryRedeliveryPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryRedeliveryPolicyViews(mutableListOf<PrimaryRedeliveryPolicy>(item, item)).size, "views")
    }
}
