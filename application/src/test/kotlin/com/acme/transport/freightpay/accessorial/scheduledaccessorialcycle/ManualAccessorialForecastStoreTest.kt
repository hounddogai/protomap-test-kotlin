package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualAccessorialForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualAccessorialForecastStore()
        val item = newManualAccessorialForecast("code-886", "label-330")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
