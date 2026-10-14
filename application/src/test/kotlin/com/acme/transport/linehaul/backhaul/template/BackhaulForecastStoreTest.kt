package com.acme.transport.linehaul.backhaul.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BackhaulForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBackhaulForecastStore()
        val item = newBackhaulForecast("code-398", "title-40")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
