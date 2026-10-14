package com.acme.transport.fleetops.defect.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefectExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefectExceptionStore()
        val item = newDefectException("code-851", "name-798")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
