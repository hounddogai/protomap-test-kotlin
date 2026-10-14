package com.acme.transport.emissions.evcharge.chargepreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackEvChargeThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackEvChargeThresholdStore()
        val item = newFallbackEvChargeThreshold("code-574", "title-52")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
