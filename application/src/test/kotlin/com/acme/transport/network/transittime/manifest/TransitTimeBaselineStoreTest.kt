package com.acme.transport.network.transittime.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TransitTimeBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTransitTimeBaselineStore()
        val item = newTransitTimeBaseline("code-554", "name-336")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
