package com.acme.transport.freightpay.accessorial.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AccessorialAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAccessorialAdjustmentStore()
        val item = newAccessorialAdjustment("code-878", "title-881")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
