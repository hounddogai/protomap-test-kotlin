package com.acme.transport.network.cutoff.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CutoffThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCutoffThresholdStore()
        val item = newCutoffThreshold("code-224", "title-481")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
