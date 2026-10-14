package com.acme.transport.freightpay.accessorial.accessorialscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalAccessorialMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalAccessorialMetricStore()
        val item = newSeasonalAccessorialMetric("code-579", "title-804")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
