package com.acme.transport.safety.violation.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ViolationForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryViolationForecastStore()
        val item = newViolationForecast("code-854", "label-1")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
