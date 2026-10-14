package com.acme.transport.safety.cameraevent.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingCameraEventForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingCameraEventForecastStore()
        val item = newPendingCameraEventForecast("code-800", "title-432")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
