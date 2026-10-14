package com.acme.transport.fleetops.tire.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveTireExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveTireExceptionStore()
        val item = newAdaptiveTireException("code-338", "label-172")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
