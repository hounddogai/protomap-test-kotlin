package com.acme.transport.fleetops.inspection.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryInspectionExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryInspectionExceptionStore()
        val item = newPrimaryInspectionException("code-114", "label-578")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
