package com.acme.transport.lastmile.proofofdelivery.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingProofOfDeliveryCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingProofOfDeliveryCycle("code-862", "title-700")
        val view = toRollingProofOfDeliveryCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingProofOfDeliveryCycleViews(mutableListOf<RollingProofOfDeliveryCycle>(item, item)).size, "views")
    }
}
