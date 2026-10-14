package com.acme.transport.crewing.qualification.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class QualificationSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryQualificationSegmentStore()
        val item = newQualificationSegment("code-698", "name-847")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
