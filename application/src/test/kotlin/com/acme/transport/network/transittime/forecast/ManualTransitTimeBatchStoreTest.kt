package com.acme.transport.network.transittime.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualTransitTimeBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualTransitTimeBatchStore()
        val item = newManualTransitTimeBatch("code-375", "title-527")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
