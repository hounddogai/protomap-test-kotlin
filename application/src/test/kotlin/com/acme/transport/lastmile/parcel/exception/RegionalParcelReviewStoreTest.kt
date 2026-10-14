package com.acme.transport.lastmile.parcel.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalParcelReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalParcelReviewStore()
        val item = newRegionalParcelReview("code-162", "name-857")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
