package com.acme.transport.freightpay.accessorial.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AccessorialBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAccessorialBaselineStore()
        val item = newAccessorialBaseline("code-3", "title-431")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
