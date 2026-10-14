package com.acme.transport.fleetops.tire.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TireOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTireOverrideStore()
        val item = newTireOverride("code-906", "label-457")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
