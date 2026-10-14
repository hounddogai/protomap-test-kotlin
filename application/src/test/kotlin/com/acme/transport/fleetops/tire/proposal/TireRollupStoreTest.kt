package com.acme.transport.fleetops.tire.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TireRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTireRollupStore()
        val item = newTireRollup("code-966", "label-745")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
