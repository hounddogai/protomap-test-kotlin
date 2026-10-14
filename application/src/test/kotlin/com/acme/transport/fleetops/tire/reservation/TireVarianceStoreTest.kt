package com.acme.transport.fleetops.tire.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TireVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTireVarianceStore()
        val item = newTireVariance("code-81", "label-722")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
