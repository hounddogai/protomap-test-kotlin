package com.acme.transport.freightpay.accessorial.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedAccessorialProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedAccessorialProfile("code-87", "name-964")
        val view = toWeightedAccessorialProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedAccessorialProfileViews(mutableListOf<WeightedAccessorialProfile>(item, item)).size, "views")
    }
}
