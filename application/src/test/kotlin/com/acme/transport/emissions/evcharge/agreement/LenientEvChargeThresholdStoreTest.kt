package com.acme.transport.emissions.evcharge.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientEvChargeThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientEvChargeThresholdStore()
        val item = newLenientEvChargeThreshold("code-333", "title-475")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
