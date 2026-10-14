package com.acme.transport.safety.nearmiss.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NearMissAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNearMissAuditStore()
        val item = newNearMissAudit("code-940", "name-443")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
