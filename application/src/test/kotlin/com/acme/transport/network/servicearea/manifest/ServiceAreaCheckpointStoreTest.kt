package com.acme.transport.network.servicearea.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServiceAreaCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServiceAreaCheckpointStore()
        val item = newServiceAreaCheckpoint("code-920", "label-834")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
