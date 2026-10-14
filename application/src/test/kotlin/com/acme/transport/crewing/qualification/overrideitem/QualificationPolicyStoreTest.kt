package com.acme.transport.crewing.qualification.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class QualificationPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryQualificationPolicyStore()
        val item = newQualificationPolicy("code-290", "label-118")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
