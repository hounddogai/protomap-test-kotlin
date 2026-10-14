package com.acme.transport.crewing.qualification.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedQualificationTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedQualificationTemplateStore()
        val item = newBatchedQualificationTemplate("code-427", "title-910")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
