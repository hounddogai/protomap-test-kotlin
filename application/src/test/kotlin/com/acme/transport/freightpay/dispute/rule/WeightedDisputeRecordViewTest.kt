package com.acme.transport.freightpay.dispute.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedDisputeRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedDisputeRecord("code-600", "label-638")
        val view = toWeightedDisputeRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedDisputeRecordViews(mutableListOf<WeightedDisputeRecord>(item, item)).size, "views")
    }
}
