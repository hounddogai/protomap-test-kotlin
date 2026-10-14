package com.acme.transport.safety.nearmiss.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledNearMissOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledNearMissOverrideStore()
        val item = newScheduledNearMissOverride("code-874", "name-302")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
