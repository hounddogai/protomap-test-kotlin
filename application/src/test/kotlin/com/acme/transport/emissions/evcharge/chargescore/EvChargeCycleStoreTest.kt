package com.acme.transport.emissions.evcharge.chargescore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargeCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargeCycleStore()
        val item = newEvChargeCycle("code-777", "label-981")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
