package com.acme.transport.fleetops.tire.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedTireTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedTireTemplateStore()
        val item = newCappedTireTemplate("code-547", "label-1")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
