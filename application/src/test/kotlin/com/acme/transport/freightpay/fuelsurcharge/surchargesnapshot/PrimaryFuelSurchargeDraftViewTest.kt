package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryFuelSurchargeDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryFuelSurchargeDraft("code-636", "name-786")
        val view = toPrimaryFuelSurchargeDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryFuelSurchargeDraftViews(mutableListOf<PrimaryFuelSurchargeDraft>(item, item)).size, "views")
    }
}
