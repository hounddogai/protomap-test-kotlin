package com.acme.transport.emissions.evcharge.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargeRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargeRequestStore()
        val item = newEvChargeRequest("code-579", "name-898")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
