package com.acme.transport.lastmile.parcel.parcelvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedParcelForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedParcelForecastStore()
        val item = newWeightedParcelForecast("code-448", "title-528")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
