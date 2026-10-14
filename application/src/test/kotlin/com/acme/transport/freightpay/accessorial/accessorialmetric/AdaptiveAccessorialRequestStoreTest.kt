package com.acme.transport.freightpay.accessorial.accessorialmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveAccessorialRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveAccessorialRequestStore()
        val item = newAdaptiveAccessorialRequest("code-559", "label-226")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
