package com.acme.transport.emissions.evcharge.pendingchargescore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargeRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargeRollupStore()
        val item = newEvChargeRollup("code-580", "name-765")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
