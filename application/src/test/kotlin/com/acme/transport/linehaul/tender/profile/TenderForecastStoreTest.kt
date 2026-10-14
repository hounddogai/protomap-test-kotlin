package com.acme.transport.linehaul.tender.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TenderForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTenderForecastStore()
        val item = newTenderForecast("code-125", "title-537")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
