package com.acme.transport.crewing.qualification.qualificationdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyQualificationPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyQualificationPolicyStore()
        val item = newLegacyQualificationPolicy("code-919", "name-260")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
