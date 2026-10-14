package com.acme.transport.emissions.evcharge.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargeOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargeOverrideStore()
        val item = newEvChargeOverride("code-179", "label-469")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
