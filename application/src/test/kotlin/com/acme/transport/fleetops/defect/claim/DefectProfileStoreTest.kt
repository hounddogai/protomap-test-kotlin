package com.acme.transport.fleetops.defect.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefectProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefectProfileStore()
        val item = newDefectProfile("code-185", "label-194")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
