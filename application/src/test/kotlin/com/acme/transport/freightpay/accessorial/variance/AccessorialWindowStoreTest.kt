package com.acme.transport.freightpay.accessorial.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AccessorialWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAccessorialWindowStore()
        val item = newAccessorialWindow("code-117", "title-763")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
