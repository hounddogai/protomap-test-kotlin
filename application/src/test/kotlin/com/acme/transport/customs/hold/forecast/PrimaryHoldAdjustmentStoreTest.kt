package com.acme.transport.customs.hold.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryHoldAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryHoldAdjustmentStore()
        val item = newPrimaryHoldAdjustment("code-141", "name-59")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
