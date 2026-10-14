package com.acme.transport.crewing.qualification.qualificationdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class QualificationEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryQualificationEntryStore()
        val item = newQualificationEntry("code-988", "label-209")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
