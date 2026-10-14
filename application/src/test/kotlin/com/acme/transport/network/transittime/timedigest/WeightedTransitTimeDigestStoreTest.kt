package com.acme.transport.network.transittime.timedigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedTransitTimeDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedTransitTimeDigestStore()
        val item = newWeightedTransitTimeDigest("code-733", "title-151")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
