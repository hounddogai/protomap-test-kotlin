package com.acme.transport.emissions.evcharge.chargepreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveEvChargeWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveEvChargeWindowStore()
        val item = newAdaptiveEvChargeWindow("code-474", "name-632")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
