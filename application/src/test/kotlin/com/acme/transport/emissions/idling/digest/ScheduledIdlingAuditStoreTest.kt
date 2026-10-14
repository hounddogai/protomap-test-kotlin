package com.acme.transport.emissions.idling.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledIdlingAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledIdlingAuditStore()
        val item = newScheduledIdlingAudit("code-177", "label-702")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
