package com.acme.transport.network.zone.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedZoneLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedZoneLedgerStore()
        val item = newWeightedZoneLedger("code-195", "title-346")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
