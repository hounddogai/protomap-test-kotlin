package com.acme.transport.fleetops.tire.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TireNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTireNoticeStore()
        val item = newTireNotice("code-304", "name-748")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
