package com.acme.transport.freightpay.fuelsurcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualFuelSurchargeScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualFuelSurchargeScheduleStore()
        val item = newManualFuelSurchargeSchedule("code-511", "name-15")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
