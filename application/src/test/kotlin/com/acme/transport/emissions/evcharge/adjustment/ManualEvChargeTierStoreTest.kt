package com.acme.transport.emissions.evcharge.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualEvChargeTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualEvChargeTierStore()
        val item = newManualEvChargeTier("code-831", "title-41")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
