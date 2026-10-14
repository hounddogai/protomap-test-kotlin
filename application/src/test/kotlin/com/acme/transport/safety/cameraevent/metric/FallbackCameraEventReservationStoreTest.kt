package com.acme.transport.safety.cameraevent.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackCameraEventReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackCameraEventReservationStore()
        val item = newFallbackCameraEventReservation("code-130", "title-173")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
