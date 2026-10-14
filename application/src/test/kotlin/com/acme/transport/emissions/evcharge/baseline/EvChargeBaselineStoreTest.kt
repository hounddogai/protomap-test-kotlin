package com.acme.transport.emissions.evcharge.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargeBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargeBaselineStore()
        val item = newEvChargeBaseline("code-489", "label-307")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
