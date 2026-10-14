package com.acme.transport.network.servicearea.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServiceAreaAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServiceAreaAuditStore()
        val item = newServiceAreaAudit("code-64", "name-370")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
