package com.acme.transport.safety.incident.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryIncidentQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryIncidentQuotaStore()
        val item = newPrimaryIncidentQuota("code-608", "name-904")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
