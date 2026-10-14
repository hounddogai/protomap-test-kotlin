package com.acme.transport.emissions.evcharge.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargeDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargeDigestStore()
        val item = newEvChargeDigest("code-382", "name-668")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
