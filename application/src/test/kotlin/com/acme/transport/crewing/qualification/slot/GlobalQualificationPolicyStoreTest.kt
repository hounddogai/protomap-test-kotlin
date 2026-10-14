package com.acme.transport.crewing.qualification.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalQualificationPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalQualificationPolicyStore()
        val item = newGlobalQualificationPolicy("code-770", "title-978")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
