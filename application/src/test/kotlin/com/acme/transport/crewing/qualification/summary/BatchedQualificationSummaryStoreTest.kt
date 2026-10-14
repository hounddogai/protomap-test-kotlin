package com.acme.transport.crewing.qualification.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedQualificationSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedQualificationSummaryStore()
        val item = newBatchedQualificationSummary("code-811", "name-820")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
