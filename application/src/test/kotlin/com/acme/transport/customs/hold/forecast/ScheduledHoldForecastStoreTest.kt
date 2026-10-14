package com.acme.transport.customs.hold.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledHoldForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledHoldForecastStore()
        val item = newScheduledHoldForecast("code-264", "label-328")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
