package com.acme.transport.freightpay.accessorial.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingAccessorialEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingAccessorialEstimateStore()
        val item = newRollingAccessorialEstimate("code-568", "title-245")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
