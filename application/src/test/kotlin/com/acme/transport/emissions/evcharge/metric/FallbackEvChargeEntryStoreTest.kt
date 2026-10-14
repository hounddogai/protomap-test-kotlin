package com.acme.transport.emissions.evcharge.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackEvChargeEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackEvChargeEntryStore()
        val item = newFallbackEvChargeEntry("code-64", "label-809")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
