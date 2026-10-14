package com.acme.transport.network.transittime.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TransitTimeClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTransitTimeClaimStore()
        val item = newTransitTimeClaim("code-391", "name-958")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
