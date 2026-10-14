package com.acme.transport.customs.clearance.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryClearanceVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryClearanceVarianceStore()
        val item = newPrimaryClearanceVariance("code-914", "title-14")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
