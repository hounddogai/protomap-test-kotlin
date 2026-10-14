package com.acme.transport.lastmile.parcel.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalParcelVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalParcelVarianceStore()
        val item = newRegionalParcelVariance("code-504", "name-863")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
