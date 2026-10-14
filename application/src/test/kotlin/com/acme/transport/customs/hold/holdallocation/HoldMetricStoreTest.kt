package com.acme.transport.customs.hold.holdallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HoldMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHoldMetricStore()
        val item = newHoldMetric("code-692", "name-273")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
