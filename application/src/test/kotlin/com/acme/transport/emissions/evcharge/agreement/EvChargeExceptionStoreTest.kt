package com.acme.transport.emissions.evcharge.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargeExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargeExceptionStore()
        val item = newEvChargeException("code-434", "label-688")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
