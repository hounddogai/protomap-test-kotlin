package com.acme.transport.customs.clearance.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledClearanceVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledClearanceVarianceStore()
        val item = newScheduledClearanceVariance("code-320", "title-803")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
