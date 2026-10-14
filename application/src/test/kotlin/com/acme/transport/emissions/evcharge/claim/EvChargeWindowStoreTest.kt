package com.acme.transport.emissions.evcharge.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargeWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargeWindowStore()
        val item = newEvChargeWindow("code-720", "label-664")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
