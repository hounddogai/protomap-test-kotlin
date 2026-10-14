package com.acme.transport.network.node.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedNodeThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedNodeThresholdStore()
        val item = newWeightedNodeThreshold("code-951", "label-163")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
