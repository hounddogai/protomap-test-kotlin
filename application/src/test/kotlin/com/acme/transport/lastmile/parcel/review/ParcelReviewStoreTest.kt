package com.acme.transport.lastmile.parcel.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ParcelReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryParcelReviewStore()
        val item = newParcelReview("code-667", "title-102")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
