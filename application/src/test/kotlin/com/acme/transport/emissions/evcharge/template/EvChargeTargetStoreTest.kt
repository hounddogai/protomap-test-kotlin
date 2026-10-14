package com.acme.transport.emissions.evcharge.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargeTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargeTargetStore()
        val item = newEvChargeTarget("code-234", "label-24")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
