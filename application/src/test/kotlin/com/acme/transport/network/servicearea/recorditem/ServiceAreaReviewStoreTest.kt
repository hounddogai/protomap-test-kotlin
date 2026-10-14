package com.acme.transport.network.servicearea.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServiceAreaReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServiceAreaReviewStore()
        val item = newServiceAreaReview("code-640", "title-778")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
