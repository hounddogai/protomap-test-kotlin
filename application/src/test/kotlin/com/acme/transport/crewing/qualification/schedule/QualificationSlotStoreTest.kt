package com.acme.transport.crewing.qualification.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class QualificationSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryQualificationSlotStore()
        val item = newQualificationSlot("code-947", "title-118")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
