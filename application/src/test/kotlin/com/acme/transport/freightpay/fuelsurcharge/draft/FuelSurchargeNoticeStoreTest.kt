package com.acme.transport.freightpay.fuelsurcharge.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelSurchargeNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelSurchargeNoticeStore()
        val item = newFuelSurchargeNotice("code-738", "title-464")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
