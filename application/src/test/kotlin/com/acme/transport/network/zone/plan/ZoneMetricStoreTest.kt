package com.acme.transport.network.zone.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ZoneMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryZoneMetricStore()
        val item = newZoneMetric("code-644", "name-388")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
