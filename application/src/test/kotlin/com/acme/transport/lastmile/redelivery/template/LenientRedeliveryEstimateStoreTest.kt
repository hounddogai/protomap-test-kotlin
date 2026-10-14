package com.acme.transport.lastmile.redelivery.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientRedeliveryEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientRedeliveryEstimateStore()
        val item = newLenientRedeliveryEstimate("code-115", "title-954")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
