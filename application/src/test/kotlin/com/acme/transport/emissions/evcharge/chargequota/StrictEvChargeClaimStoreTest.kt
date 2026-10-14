package com.acme.transport.emissions.evcharge.chargequota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictEvChargeClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictEvChargeClaimStore()
        val item = newStrictEvChargeClaim("code-101", "name-177")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
