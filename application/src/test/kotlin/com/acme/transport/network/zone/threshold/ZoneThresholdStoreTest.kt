package com.acme.transport.network.zone.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ZoneThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryZoneThresholdStore()
        val item = newZoneThreshold("code-681", "label-941")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
