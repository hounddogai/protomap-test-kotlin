package com.acme.transport.network.zone.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredZonePlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredZonePlanStore()
        val item = newTieredZonePlan("code-194", "title-481")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
