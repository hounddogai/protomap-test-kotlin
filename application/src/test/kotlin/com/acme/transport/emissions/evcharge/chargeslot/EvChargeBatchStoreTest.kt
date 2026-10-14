package com.acme.transport.emissions.evcharge.chargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargeBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargeBatchStore()
        val item = newEvChargeBatch("code-217", "name-16")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
