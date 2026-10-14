package com.acme.transport.emissions.evcharge.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryEvChargeDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryEvChargeDigestStore()
        val item = newPrimaryEvChargeDigest("code-869", "name-666")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
