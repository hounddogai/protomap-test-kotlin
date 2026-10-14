package com.acme.transport.customs.inspection.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionDraftStore()
        val item = newInspectionDraft("code-966", "name-452")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
