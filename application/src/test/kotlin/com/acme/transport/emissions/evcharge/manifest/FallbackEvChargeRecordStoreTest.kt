package com.acme.transport.emissions.evcharge.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackEvChargeRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackEvChargeRecordStore()
        val item = newFallbackEvChargeRecord("code-824", "title-2")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
