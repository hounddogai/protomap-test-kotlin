package com.acme.transport.fleetops.inspection.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackInspectionNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackInspectionNoticeStore()
        val item = newFallbackInspectionNotice("code-314", "name-877")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
