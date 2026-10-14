package com.acme.transport.freightpay.fuelsurcharge.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelSurchargeTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelSurchargeTemplateStore()
        val item = newFuelSurchargeTemplate("code-499", "label-678")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
