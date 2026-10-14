package com.acme.transport.emissions.evcharge.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultEvChargeGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultEvChargeGroupStore()
        val item = newDefaultEvChargeGroup("code-11", "title-251")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
