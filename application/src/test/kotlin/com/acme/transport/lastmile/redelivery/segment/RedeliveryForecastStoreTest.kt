package com.acme.transport.lastmile.redelivery.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RedeliveryForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRedeliveryForecastStore()
        val item = newRedeliveryForecast("code-679", "title-633")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
