package com.acme.transport.network.transittime.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TransitTimeVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTransitTimeVarianceStore()
        val item = newTransitTimeVariance("code-753", "title-789")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
