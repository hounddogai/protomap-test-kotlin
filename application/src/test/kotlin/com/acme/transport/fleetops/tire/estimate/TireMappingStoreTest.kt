package com.acme.transport.fleetops.tire.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TireMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTireMappingStore()
        val item = newTireMapping("code-197", "name-223")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
