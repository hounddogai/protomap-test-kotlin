package com.acme.transport.freightpay.fuelsurcharge.surchargewindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledFuelSurchargeWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledFuelSurchargeWindowStore()
        val item = newScheduledFuelSurchargeWindow("code-894", "name-875")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
