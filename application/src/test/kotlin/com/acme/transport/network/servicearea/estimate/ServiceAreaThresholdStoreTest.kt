package com.acme.transport.network.servicearea.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServiceAreaThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServiceAreaThresholdStore()
        val item = newServiceAreaThreshold("code-823", "name-768")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
