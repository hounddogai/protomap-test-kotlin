package com.acme.transport.network.transittime.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyTransitTimeRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyTransitTimeRollupStore()
        val item = newLegacyTransitTimeRollup("code-265", "label-126")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
