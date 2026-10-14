package com.acme.transport.freightpay.freightinvoice.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FreightInvoiceForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFreightInvoiceForecastStore()
        val item = newFreightInvoiceForecast("code-52", "name-681")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
