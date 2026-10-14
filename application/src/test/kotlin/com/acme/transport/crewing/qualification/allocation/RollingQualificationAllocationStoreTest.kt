package com.acme.transport.crewing.qualification.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingQualificationAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingQualificationAllocationStore()
        val item = newRollingQualificationAllocation("code-622", "title-345")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
