package com.acme.transport.emissions.evcharge.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargeEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargeEstimateStore()
        val item = newEvChargeEstimate("code-661", "name-916")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
