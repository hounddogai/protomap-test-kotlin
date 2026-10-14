package com.acme.transport.linehaul.capacity.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryCapacityThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryCapacityThresholdStore()
        val item = newPrimaryCapacityThreshold("code-52", "name-160")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
