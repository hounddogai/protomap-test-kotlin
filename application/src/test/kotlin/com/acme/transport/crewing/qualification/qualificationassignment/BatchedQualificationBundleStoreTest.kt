package com.acme.transport.crewing.qualification.qualificationassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedQualificationBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedQualificationBundleStore()
        val item = newBatchedQualificationBundle("code-704", "label-462")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
