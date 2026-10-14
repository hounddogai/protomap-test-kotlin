package com.acme.transport.emissions.idling.idlingcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackIdlingCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackIdlingCheckpointStore()
        val item = newFallbackIdlingCheckpoint("code-809", "name-303")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
