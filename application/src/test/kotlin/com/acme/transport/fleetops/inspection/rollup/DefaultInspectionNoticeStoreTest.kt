package com.acme.transport.fleetops.inspection.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultInspectionNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultInspectionNoticeStore()
        val item = newDefaultInspectionNotice("code-288", "label-898")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
