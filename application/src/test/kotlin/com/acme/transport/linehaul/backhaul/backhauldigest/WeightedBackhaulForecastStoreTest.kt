package com.acme.transport.linehaul.backhaul.backhauldigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedBackhaulForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedBackhaulForecastStore()
        val item = newWeightedBackhaulForecast("code-736", "title-277")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
