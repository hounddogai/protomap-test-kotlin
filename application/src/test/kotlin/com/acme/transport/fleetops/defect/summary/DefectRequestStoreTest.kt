package com.acme.transport.fleetops.defect.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefectRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefectRequestStore()
        val item = newDefectRequest("code-801", "name-662")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
