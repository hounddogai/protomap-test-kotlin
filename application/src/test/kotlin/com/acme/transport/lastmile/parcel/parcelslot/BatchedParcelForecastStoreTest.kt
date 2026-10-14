package com.acme.transport.lastmile.parcel.parcelslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedParcelForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedParcelForecastStore()
        val item = newBatchedParcelForecast("code-107", "title-999")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
