package com.acme.transport.crewing.qualification.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalQualificationDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalQualificationDraftStore()
        val item = newRegionalQualificationDraft("code-522", "name-550")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
