package com.acme.transport.freightpay.accessorial.accessorialbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveAccessorialSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveAccessorialSlotStore()
        val item = newAdaptiveAccessorialSlot("code-346", "name-12")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
