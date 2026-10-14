package com.acme.transport.emissions.evcharge.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargeMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargeMappingStore()
        val item = newEvChargeMapping("code-566", "name-912")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
