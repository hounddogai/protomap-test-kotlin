package com.acme.transport.network.cutoff.cutoffsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CutoffForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCutoffForecastStore()
        val item = newCutoffForecast("code-679", "title-620")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
