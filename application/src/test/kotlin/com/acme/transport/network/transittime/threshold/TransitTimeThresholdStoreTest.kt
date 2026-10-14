package com.acme.transport.network.transittime.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TransitTimeThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTransitTimeThresholdStore()
        val item = newTransitTimeThreshold("code-603", "title-470")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
