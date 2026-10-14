package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultTrailerMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultTrailerMetricStore()
        val item = newDefaultTrailerMetric("code-817", "name-457")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
