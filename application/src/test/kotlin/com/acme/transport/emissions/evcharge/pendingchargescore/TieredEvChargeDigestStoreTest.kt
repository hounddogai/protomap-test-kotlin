package com.acme.transport.emissions.evcharge.pendingchargescore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredEvChargeDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredEvChargeDigestStore()
        val item = newTieredEvChargeDigest("code-193", "name-549")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
