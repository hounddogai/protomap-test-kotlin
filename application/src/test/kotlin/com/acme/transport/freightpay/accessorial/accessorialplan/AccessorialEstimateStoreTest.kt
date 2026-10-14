package com.acme.transport.freightpay.accessorial.accessorialplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AccessorialEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAccessorialEstimateStore()
        val item = newAccessorialEstimate("code-961", "title-650")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
