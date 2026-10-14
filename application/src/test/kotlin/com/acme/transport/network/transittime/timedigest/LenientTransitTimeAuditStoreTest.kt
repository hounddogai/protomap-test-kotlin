package com.acme.transport.network.transittime.timedigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientTransitTimeAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientTransitTimeAuditStore()
        val item = newLenientTransitTimeAudit("code-850", "title-934")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
