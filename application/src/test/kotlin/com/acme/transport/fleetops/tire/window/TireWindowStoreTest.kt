package com.acme.transport.fleetops.tire.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TireWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTireWindowStore()
        val item = newTireWindow("code-629", "title-243")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
