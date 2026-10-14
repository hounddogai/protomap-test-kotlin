package com.acme.transport.safety.violation.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveViolationAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveViolationAuditStore()
        val item = newAdaptiveViolationAudit("code-317", "name-944")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
