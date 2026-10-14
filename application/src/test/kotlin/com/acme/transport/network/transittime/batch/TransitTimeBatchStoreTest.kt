package com.acme.transport.network.transittime.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TransitTimeBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTransitTimeBatchStore()
        val item = newTransitTimeBatch("code-436", "name-705")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
