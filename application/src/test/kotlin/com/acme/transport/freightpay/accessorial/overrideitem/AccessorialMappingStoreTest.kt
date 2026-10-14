package com.acme.transport.freightpay.accessorial.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AccessorialMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAccessorialMappingStore()
        val item = newAccessorialMapping("code-567", "label-451")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
