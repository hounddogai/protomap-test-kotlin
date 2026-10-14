package com.acme.transport.linehaul.tender.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalTenderForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalTenderForecastStore()
        val item = newGlobalTenderForecast("code-359", "label-173")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
