package com.acme.transport.lastmile.drop.dropslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DropEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDropEstimateStore()
        val item = newDropEstimate("code-998", "label-222")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
