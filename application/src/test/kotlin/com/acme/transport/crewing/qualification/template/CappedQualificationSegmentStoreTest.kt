package com.acme.transport.crewing.qualification.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedQualificationSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedQualificationSegmentStore()
        val item = newCappedQualificationSegment("code-775", "title-654")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
