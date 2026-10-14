package com.acme.transport.network.node.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveNodeThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveNodeThresholdStore()
        val item = newAdaptiveNodeThreshold("code-543", "name-342")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
