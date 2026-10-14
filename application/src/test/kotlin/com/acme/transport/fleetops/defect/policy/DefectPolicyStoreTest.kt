package com.acme.transport.fleetops.defect.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefectPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefectPolicyStore()
        val item = newDefectPolicy("code-424", "title-366")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
