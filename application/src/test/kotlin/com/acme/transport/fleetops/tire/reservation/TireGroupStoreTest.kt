package com.acme.transport.fleetops.tire.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TireGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTireGroupStore()
        val item = newTireGroup("code-552", "name-917")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
