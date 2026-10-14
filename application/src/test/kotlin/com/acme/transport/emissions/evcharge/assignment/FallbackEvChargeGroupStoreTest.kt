package com.acme.transport.emissions.evcharge.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackEvChargeGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackEvChargeGroupStore()
        val item = newFallbackEvChargeGroup("code-660", "title-67")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
