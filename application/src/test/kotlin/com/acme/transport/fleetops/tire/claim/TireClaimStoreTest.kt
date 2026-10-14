package com.acme.transport.fleetops.tire.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TireClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTireClaimStore()
        val item = newTireClaim("code-992", "label-798")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
