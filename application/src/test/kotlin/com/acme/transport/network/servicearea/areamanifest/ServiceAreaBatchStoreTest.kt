package com.acme.transport.network.servicearea.areamanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServiceAreaBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServiceAreaBatchStore()
        val item = newServiceAreaBatch("code-279", "name-933")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
