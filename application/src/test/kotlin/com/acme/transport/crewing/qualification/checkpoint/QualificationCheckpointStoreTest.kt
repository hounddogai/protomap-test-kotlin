package com.acme.transport.crewing.qualification.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class QualificationCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryQualificationCheckpointStore()
        val item = newQualificationCheckpoint("code-48", "title-228")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
