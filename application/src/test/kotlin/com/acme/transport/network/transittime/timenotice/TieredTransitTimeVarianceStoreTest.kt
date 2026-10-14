package com.acme.transport.network.transittime.timenotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredTransitTimeVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredTransitTimeVarianceStore()
        val item = newTieredTransitTimeVariance("code-85", "name-576")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
