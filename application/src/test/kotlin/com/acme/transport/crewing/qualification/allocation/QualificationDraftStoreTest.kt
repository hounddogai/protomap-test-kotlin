package com.acme.transport.crewing.qualification.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class QualificationDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryQualificationDraftStore()
        val item = newQualificationDraft("code-891", "label-493")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
