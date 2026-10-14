package com.acme.transport.lastmile.redelivery.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalRedeliveryEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalRedeliveryEstimateStore()
        val item = newGlobalRedeliveryEstimate("code-483", "title-76")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
