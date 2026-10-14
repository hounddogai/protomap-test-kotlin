package com.acme.transport.fleetops.defect.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefectTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefectTargetStore()
        val item = newDefectTarget("code-505", "title-154")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
