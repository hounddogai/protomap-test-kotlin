package com.acme.transport.fleetops.defect.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingDefectExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingDefectExceptionStore()
        val item = newRollingDefectException("code-599", "title-391")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
