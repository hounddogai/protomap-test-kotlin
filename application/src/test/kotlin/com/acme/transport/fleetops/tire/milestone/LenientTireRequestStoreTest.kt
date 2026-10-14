package com.acme.transport.fleetops.tire.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientTireRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientTireRequestStore()
        val item = newLenientTireRequest("code-164", "title-940")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
