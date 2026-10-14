package com.acme.transport.lastmile.parcel.parcelvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultParcelVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultParcelVarianceStore()
        val item = newDefaultParcelVariance("code-22", "name-534")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
