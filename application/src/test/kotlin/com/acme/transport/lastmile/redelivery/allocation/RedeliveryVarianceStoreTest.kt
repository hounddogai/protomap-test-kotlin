package com.acme.transport.lastmile.redelivery.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RedeliveryVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRedeliveryVarianceStore()
        val item = newRedeliveryVariance("code-631", "title-888")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
