package com.acme.transport.fleetops.inspection.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionNoticeStore()
        val item = newInspectionNotice("code-661", "name-105")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
