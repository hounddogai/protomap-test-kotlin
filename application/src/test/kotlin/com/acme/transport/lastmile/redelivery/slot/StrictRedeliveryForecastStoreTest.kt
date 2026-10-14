package com.acme.transport.lastmile.redelivery.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictRedeliveryForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictRedeliveryForecastStore()
        val item = newStrictRedeliveryForecast("code-367", "label-688")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
