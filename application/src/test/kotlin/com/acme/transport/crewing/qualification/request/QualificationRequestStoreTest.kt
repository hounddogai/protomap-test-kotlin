package com.acme.transport.crewing.qualification.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class QualificationRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryQualificationRequestStore()
        val item = newQualificationRequest("code-635", "label-278")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
