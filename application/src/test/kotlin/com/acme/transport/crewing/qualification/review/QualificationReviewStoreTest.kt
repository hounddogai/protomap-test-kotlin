package com.acme.transport.crewing.qualification.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class QualificationReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryQualificationReviewStore()
        val item = newQualificationReview("code-791", "name-541")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
