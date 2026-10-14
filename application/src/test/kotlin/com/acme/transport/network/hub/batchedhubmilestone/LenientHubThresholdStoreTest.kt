package com.acme.transport.network.hub.batchedhubmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientHubThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientHubThresholdStore()
        val item = newLenientHubThreshold("code-981", "name-185")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
