package com.acme.transport.crewing.swap.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveSwapWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveSwapWindowStore()
        val item = newAdaptiveSwapWindow("code-792", "label-664")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
