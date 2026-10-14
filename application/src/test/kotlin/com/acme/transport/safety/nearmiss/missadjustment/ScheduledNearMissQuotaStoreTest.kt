package com.acme.transport.safety.nearmiss.missadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledNearMissQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledNearMissQuotaStore()
        val item = newScheduledNearMissQuota("code-515", "name-201")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
