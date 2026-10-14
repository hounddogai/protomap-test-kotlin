package com.acme.transport.safety.cameraevent.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedCameraEventTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedCameraEventTierStore()
        val item = newWeightedCameraEventTier("code-935", "name-471")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
