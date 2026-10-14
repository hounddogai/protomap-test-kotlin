package com.acme.transport.crewing.qualification.tieredqualificationdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedQualificationCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedQualificationCheckpointStore()
        val item = newBatchedQualificationCheckpoint("code-605", "title-187")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
