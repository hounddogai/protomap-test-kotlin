package com.acme.transport.network.servicearea.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveServiceAreaCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveServiceAreaCheckpointStore()
        val item = newAdaptiveServiceAreaCheckpoint("code-907", "title-690")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
