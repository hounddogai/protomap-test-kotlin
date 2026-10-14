package com.acme.transport.fleetops.tire.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TireProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTireProfileStore()
        val item = newTireProfile("code-671", "name-614")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
