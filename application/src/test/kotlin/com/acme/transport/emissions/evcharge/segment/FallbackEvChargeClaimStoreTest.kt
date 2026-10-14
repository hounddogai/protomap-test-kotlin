package com.acme.transport.emissions.evcharge.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackEvChargeClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackEvChargeClaimStore()
        val item = newFallbackEvChargeClaim("code-256", "title-468")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
