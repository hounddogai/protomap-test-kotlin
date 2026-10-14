package com.acme.transport.fleetops.tire.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TireBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTireBaselineStore()
        val item = newTireBaseline("code-293", "title-499")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
