package com.acme.transport.emissions.idling.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IdlingThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIdlingThresholdStore()
        val item = newIdlingThreshold("code-907", "title-709")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
