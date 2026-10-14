package com.acme.transport.lastmile.parcel.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ParcelVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryParcelVarianceStore()
        val item = newParcelVariance("code-237", "name-496")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
