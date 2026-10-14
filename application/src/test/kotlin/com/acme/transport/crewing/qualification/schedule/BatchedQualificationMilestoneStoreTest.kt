package com.acme.transport.crewing.qualification.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedQualificationMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedQualificationMilestoneStore()
        val item = newBatchedQualificationMilestone("code-623", "title-367")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
