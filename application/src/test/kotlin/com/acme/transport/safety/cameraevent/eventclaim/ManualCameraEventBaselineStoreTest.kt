package com.acme.transport.safety.cameraevent.eventclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualCameraEventBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualCameraEventBaselineStore()
        val item = newManualCameraEventBaseline("code-111", "name-458")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
