package com.acme.transport.fleetops.defect.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefectGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefectGroupStore()
        val item = newDefectGroup("code-17", "title-668")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
