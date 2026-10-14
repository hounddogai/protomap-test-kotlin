package com.acme.transport.network.zone.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ZoneBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryZoneBatchStore()
        val item = newZoneBatch("code-106", "title-692")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
