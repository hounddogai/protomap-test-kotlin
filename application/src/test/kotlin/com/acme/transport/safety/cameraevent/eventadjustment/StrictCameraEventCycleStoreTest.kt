package com.acme.transport.safety.cameraevent.eventadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictCameraEventCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictCameraEventCycleStore()
        val item = newStrictCameraEventCycle("code-520", "title-720")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
