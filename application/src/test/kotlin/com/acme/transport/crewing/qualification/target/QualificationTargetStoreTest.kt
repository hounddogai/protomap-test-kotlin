package com.acme.transport.crewing.qualification.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class QualificationTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryQualificationTargetStore()
        val item = newQualificationTarget("code-798", "title-724")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
