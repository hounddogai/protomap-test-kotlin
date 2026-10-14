package com.acme.transport.network.transittime.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TransitTimeMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTransitTimeMappingStore()
        val item = newTransitTimeMapping("code-361", "title-886")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
