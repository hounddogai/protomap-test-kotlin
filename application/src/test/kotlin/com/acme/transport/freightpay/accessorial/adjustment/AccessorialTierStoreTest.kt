package com.acme.transport.freightpay.accessorial.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AccessorialTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAccessorialTierStore()
        val item = newAccessorialTier("code-309", "label-866")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
