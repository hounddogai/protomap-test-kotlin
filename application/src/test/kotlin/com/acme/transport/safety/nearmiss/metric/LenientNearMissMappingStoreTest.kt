package com.acme.transport.safety.nearmiss.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientNearMissMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientNearMissMappingStore()
        val item = newLenientNearMissMapping("code-835", "name-239")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
